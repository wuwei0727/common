#!/bin/bash
​
function stopService() {
    local target="$1"
    if [[ "$(systemctl is-active $target)" == "active" ]]; then
        systemctl stop ${target}
    fi
}
​
function checkVersion() {
    localDockerVersion=$(docker -v 2>/dev/null | grep -oP '\d+\.\d+\.\d+')
    if [[ "$localDockerVersion" == "$onlineVersion" ]]; then
        echo "版本不需要更新，云端版本：$onlineVersion，本地版本：$localDockerVersion"
        exit 0
    else
        echo "版本需要更新，云端版本：$onlineVersion，本地版本：$localDockerVersion"
    fi
}
​
# --------------------------------------
command -v yum &>/dev/null && PI="yum"
command -v apt &>/dev/null && PI="apt"
[ -z "$PI" ] && echo "请检查系统是否安装了yum或者apt-get" && exit 1
​
command -v wget &>/dev/null || ${PI} install wget -y || {
    echo "请检查系统是否安装了wget" && exit 1
}
command -v iptables &>/dev/null || ${PI} install wget -y || {
    echo "请检查系统是否安装了iptables" && exit 1
}
​
# 现在的内核已经包含device-mapper-persistent-data，lvm2，一般不用安装
$PI install -q -y device-mapper-persistent-data lvm2 &>/dev/null || {
    echo "请检查系统是否安装了device-mapper-persistent-data和lvm2" && exit 1
}
​
# 定义版本
cd /tmp
install_path="/data"
# target='25.0.4'
versionList=$(curl -Ss "https://mirrors.ustc.edu.cn/docker-ce/linux/static/stable/x86_64/" | grep -oE 'docker-.*.tgz' | cut -d '"' -f 1)
onlineVersion="$(echo "$versionList" | tail -1 | grep -oP '\d+\.\d+\.\d+')"
# 检查本地和云端的版本
checkVersion
# 关闭服务
stopService "docker"
stopService "docker.socket"
stopService "containerd"
# 下载docker二进制文件
# wget -O docker-${target}.tgz https://mirrors.ustc.edu.cn/docker-ce/linux/static/stable/x86_64/docker-${target}.tgz
# wget -O docker-rootless-extras-${target}.tgz https://mirrors.ustc.edu.cn/docker-ce/linux/static/stable/x86_64/docker-rootless-extras-${target}.tgz
wget -O docker-${onlineVersion}.tgz https://mirrors.ustc.edu.cn/docker-ce/linux/static/stable/x86_64/docker-${onlineVersion}.tgz
wget -O docker-rootless-extras-${onlineVersion}.tgz https://mirrors.ustc.edu.cn/docker-ce/linux/static/stable/x86_64/docker-rootless-extras-${onlineVersion}.tgz
ls *.tgz | xargs -I {} tar -xf {}
chmod +x ./docker/*
chmod +x ./docker-rootless-extras/*
\mv docker/* /usr/bin/
\mv docker-rootless-extras/* /usr/bin/
rm -rf /tmp/docker /tmp/docker-rootless-extras /tmp/docker-${onlineVersion}.tgz /tmp/docker-rootless-extras-${onlineVersion}.tgz
​
# 创建docker用户组
groupadd docker 2>/dev/null
​
# 创建containerd的service文件
cat >/usr/lib/systemd/system/containerd.service <<EOF
[Unit]
Description=containerd container runtime
Documentation=https://containerd.io
After=network.target
​
[Service]
ExecStartPre=-/sbin/modprobe overlay
ExecStart=/usr/bin/containerd
Type=notify
Delegate=yes
KillMode=process
Restart=always
RestartSec=5
LimitNPROC=infinity
LimitCORE=infinity
LimitNOFILE=1048576
TasksMax=infinity
OOMScoreAdjust=-999
​
[Install]
WantedBy=multi-user.target
EOF
​
# docker.socket
cat >/usr/lib/systemd/system/docker.socket <<EOF
[Unit]
Description=Docker Socket for the API
​
[Socket]
ListenStream=/var/run/docker.sock
SocketMode=0660
SocketUser=root
SocketGroup=docker
​
[Install]
WantedBy=sockets.target
EOF
​
# docker.service
cat >/usr/lib/systemd/system/docker.service <<"EOF"
[Unit]
Description=Docker Application Container Engine
Documentation=https://docs.docker.com
After=network-online.target firewalld.service containerd.service
Wants=network-online.target
Requires=docker.socket containerd.service
​
[Service]
Type=notify
ExecStart=/usr/bin/dockerd -H fd:// --containerd=/run/containerd/containerd.sock
ExecReload=/bin/kill -s HUP $MAINPID
TimeoutSec=0
RestartSec=2
Restart=always
StartLimitBurst=3
StartLimitInterval=60s
LimitNOFILE=infinity
LimitNPROC=infinity
LimitCORE=infinity
TasksMax=infinity
Delegate=yes
KillMode=process
OOMScoreAdjust=-500
​
[Install]
WantedBy=multi-user.target
EOF
​
# ipv4转发
if [ "$(cat /proc/sys/net/ipv4/ip_forward)" != "1" ]; then
    sed -i '/net.ipv4.ip_forward/d' /etc/sysctl.conf
    echo "net.ipv4.ip_forward=1" >>/etc/sysctl.conf
    sysctl -p
fi
​
# 配置
mkdir -p /etc/docker
cat >/etc/docker/daemon.json <<EOF
{
    "registry-mirrors": [
        "https://docker.nju.edu.cn",
        "https://docker.mirrors.ustc.edu.cn",
        "https://hub-mirror.c.163.com"
        ],
    "data-root": "$install_path/docker",
    "log-driver": "json-file",
    "log-opts": {
        "max-size": "500m",
        "max-file": "3"
    }
}
EOF
​
# 启动docker
systemctl daemon-reload
systemctl enable --now containerd docker.socket docker.service

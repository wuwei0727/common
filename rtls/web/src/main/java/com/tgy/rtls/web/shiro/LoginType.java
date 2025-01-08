package com.tgy.rtls.web.shiro;

/**
 * @BelongsProject: 智慧停车场
 * @BelongsPackage: com.tgy.rtls.web.shiro
 * @Author: wuwei
 * @CreateTime: 2022-07-29 16:43
 * @Description: TODO
 * @Version: 1.0
 */
public enum LoginType {
        WEB("web"), SMALLAPP("smallApp");

        private String type;

        private LoginType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return this.type.toString();
        }

}

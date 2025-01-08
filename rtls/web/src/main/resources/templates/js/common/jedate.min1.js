/**
 @Name : jeDate v6.5.0 日期控件
 @Author: chen guojun
 @Date: 2018-04-30
 @QQ群：516754269
 @官网：http://www.jemui.com/ 或 https://github.com/singod/jeDate
 */
!function (a, b) {
    "function" == typeof define && define.amd ? define(b) : "object" == typeof exports ? module.exports = b() : a.jeDate = b()
}(this, function () {
    function DateTime(a, b) {
        var c = this, d = new Date, e = ["FullYear", "Month", "Date", "Hours", "Minutes", "Seconds"], f = jet.extend({
            YYYY: null,
            MM: null,
            DD: null,
            hh: d.getHours(),
            mm: d.getMinutes(),
            ss: d.getSeconds()
        }, b), g = void 0 == b ? d : new Date(f.YYYY, f.MM, f.DD, f.hh, f.mm, f.ss);
        (a || []).length > 0 && jet.each(a, function (a, b) {
            g["set" + e[a]]("Month" == e[a] ? parseInt(b) - 1 : parseInt(b))
        }), c.reDate = function () {
            return new DateTime
        }, c.GetValue = function () {
            return g
        }, c.GetYear = function () {
            return g.getFullYear()
        }, c.GetMonth = function () {
            return g.getMonth() + 1
        }, c.GetDate = function () {
            return g.getDate()
        }, c.GetHours = function () {
            return g.getHours()
        }, c.GetMinutes = function () {
            return g.getMinutes()
        }, c.GetSeconds = function () {
            return g.getSeconds()
        }
    }

    function jeDatePick(a, b) {
        var c = {
            language: {
                name: "cn",
                month: ["01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"],
                weeks: ["日", "一", "二", "三", "四", "五", "六"],
                times: ["小时", "分钟", "秒数"],
                timetxt: ["时间选择", "开始时间", "结束时间"],
                backtxt: "返回日期",
                clear: "关闭",
                today: "现在",
                yes: "确定"
            },
            format: "YYYY-MM-DD hh:mm:ss",
            minDate: "1900-01-01 00:00:00",
            maxDate: "2099-12-31 23:59:59",
            isShow: !0,
            multiPane: !0,
            onClose: !0,
            range: !1,
            trigger: "click",
            position: [],
            valiDate: [],
            isinitVal: !1,
            initDate: {},
            isTime: !0,
            isClear: !0,
            isToday: !0,
            isYes: !0,
            festival: !1,
            fixed: !0,
            zIndex: 2099,
            method: {},
            theme: {},
            shortcut: [],
            donefun: null,
            before: null,
            succeed: null
        };
        this.$opts = jet.extend(c, b || {}), this.valCell = $Q(a), this.format = this.$opts.format, null != this.valCell ? this.init() : alert(a + "  ID或类名不存在!"), jet.extend(this, this.$opts.method), delete this.$opts.method
    }

    function jeLunar(a, b, c) {
        function o(a) {
            var w, b = function (a, b) {
                var c = new Date(31556925974.7 * (a - 1900) + 6e4 * e[b] + Date.UTC(1900, 0, 6, 2, 5));
                return c.getUTCDate()
            }, c = function (a) {
                var b, c = 348;
                for (b = 32768; b > 8; b >>= 1) c += d[a - 1900] & b ? 1 : 0;
                return c + p(a)
            }, o = function (a) {
                return f.charAt(a % 10) + g.charAt(a % 12)
            }, p = function (a) {
                var b = q(a) ? 65536 & d[a - 1900] ? 30 : 29 : 0;
                return b
            }, q = function (a) {
                return 15 & d[a - 1900]
            }, r = function (a, b) {
                return d[a - 1900] & 65536 >> b ? 30 : 29
            }, s = function (a) {
                var b, d = 0, e = 0, f = new Date(1900, 0, 31), g = (a - f) / 864e5;
                for (this.dayCyl = g + 40, this.monCyl = 14, b = 1900; 2050 > b && g > 0; b++) e = c(b), g -= e, this.monCyl += 12;
                for (0 > g && (g += e, b--, this.monCyl -= 12), this.year = b, this.yearCyl = b - 1864, d = q(b), this.isLeap = !1, b = 1; 13 > b && g > 0; b++) d > 0 && b == d + 1 && 0 == this.isLeap ? (--b, this.isLeap = !0, e = p(this.year)) : e = r(this.year, b), 1 == this.isLeap && b == d + 1 && (this.isLeap = !1), g -= e, 0 == this.isLeap && this.monCyl++;
                0 == g && d > 0 && b == d + 1 && (this.isLeap ? this.isLeap = !1 : (this.isLeap = !0, --b, --this.monCyl)), 0 > g && (g += e, --b, --this.monCyl), this.month = b, this.day = g + 1
            }, t = function (a) {
                return 10 > a ? "0" + (0 | a) : a
            }, u = function (a, b) {
                var c = a;
                return b.replace(/dd?d?d?|MM?M?M?|yy?y?y?/g, function (a) {
                    switch (a) {
                        case"yyyy":
                            var b = "000" + c.getFullYear();
                            return b.substring(b.length - 4);
                        case"dd":
                            return t(c.getDate());
                        case"d":
                            return c.getDate().toString();
                        case"MM":
                            return t(c.getMonth() + 1);
                        case"M":
                            return c.getMonth() + 1
                    }
                })
            }, v = function (a, b) {
                var c;
                switch (b) {
                    case 10:
                        c = "初十";
                        break;
                    case 20:
                        c = "二十";
                        break;
                    case 30:
                        c = "三十";
                        break;
                    default:
                        c = k.charAt(Math.floor(b / 10)), c += j.charAt(b % 10)
                }
                return c
            };
            this.isToday = !1, this.isRestDay = !1, this.solarYear = u(a, "yyyy"), this.solarMonth = u(a, "M"), this.solarDate = u(a, "d"), this.solarWeekDay = a.getDay(), this.inWeekDays = "星期" + j.charAt(this.solarWeekDay), w = new s(a), this.lunarYear = w.year, this.shengxiao = h.charAt((this.lunarYear - 4) % 12), this.lunarMonth = w.month, this.lunarIsLeapMonth = w.isLeap, this.lnongMonth = this.lunarIsLeapMonth ? "闰" + l[w.month - 1] : l[w.month - 1], this.lunarDate = w.day, this.showInLunar = this.lnongDate = v(this.lunarMonth, this.lunarDate), 1 == this.lunarDate && (this.showInLunar = this.lnongMonth + "月"), this.ganzhiYear = o(w.yearCyl), this.ganzhiMonth = o(w.monCyl), this.ganzhiDate = o(w.dayCyl++), this.jieqi = "", this.restDays = 0, b(this.solarYear, 2 * (this.solarMonth - 1)) == u(a, "d") && (this.showInLunar = this.jieqi = i[2 * (this.solarMonth - 1)]), b(this.solarYear, 2 * (this.solarMonth - 1) + 1) == u(a, "d") && (this.showInLunar = this.jieqi = i[2 * (this.solarMonth - 1) + 1]), "清明" == this.showInLunar && (this.showInLunar = "清明节", this.restDays = 1), this.solarFestival = m[u(a, "MM") + u(a, "dd")], "undefined" == typeof this.solarFestival ? this.solarFestival = "" : /\*(\d)/.test(this.solarFestival) && (this.restDays = parseInt(RegExp.$1), this.solarFestival = this.solarFestival.replace(/\*\d/, "")), this.showInLunar = "" == this.solarFestival ? this.showInLunar : this.solarFestival, this.lunarFestival = n[this.lunarIsLeapMonth ? "00" : t(this.lunarMonth) + t(this.lunarDate)], "undefined" == typeof this.lunarFestival ? this.lunarFestival = "" : /\*(\d)/.test(this.lunarFestival) && (this.restDays = this.restDays > parseInt(RegExp.$1) ? this.restDays : parseInt(RegExp.$1), this.lunarFestival = this.lunarFestival.replace(/\*\d/, "")), 12 == this.lunarMonth && this.lunarDate == r(this.lunarYear, 12) && (this.lunarFestival = n["0100"], this.restDays = 1), this.showInLunar = "" == this.lunarFestival ? this.showInLunar : this.lunarFestival
        }

        var d = [19416, 19168, 42352, 21717, 53856, 55632, 91476, 22176, 39632, 21970, 19168, 42422, 42192, 53840, 119381, 46400, 54944, 44450, 38320, 84343, 18800, 42160, 46261, 27216, 27968, 109396, 11104, 38256, 21234, 18800, 25958, 54432, 59984, 28309, 23248, 11104, 100067, 37600, 116951, 51536, 54432, 120998, 46416, 22176, 107956, 9680, 37584, 53938, 43344, 46423, 27808, 46416, 86869, 19872, 42448, 83315, 21200, 43432, 59728, 27296, 44710, 43856, 19296, 43748, 42352, 21088, 62051, 55632, 23383, 22176, 38608, 19925, 19152, 42192, 54484, 53840, 54616, 46400, 46496, 103846, 38320, 18864, 43380, 42160, 45690, 27216, 27968, 44870, 43872, 38256, 19189, 18800, 25776, 29859, 59984, 27480, 21952, 43872, 38613, 37600, 51552, 55636, 54432, 55888, 30034, 22176, 43959, 9680, 37584, 51893, 43344, 46240, 47780, 44368, 21977, 19360, 42416, 86390, 21168, 43312, 31060, 27296, 44368, 23378, 19296, 42726, 42208, 53856, 60005, 54576, 23200, 30371, 38608, 19415, 19152, 42192, 118966, 53840, 54560, 56645, 46496, 22224, 21938, 18864, 42359, 42160, 43600, 111189, 27936, 44448],
            e = [0, 21208, 43467, 63836, 85337, 107014, 128867, 150921, 173149, 195551, 218072, 240693, 263343, 285989, 308563, 331033, 353350, 375494, 397447, 419210, 440795, 462224, 483532, 504758],
            f = "甲乙丙丁戊己庚辛壬癸", g = "子丑寅卯辰巳午未申酉戌亥", h = "鼠牛虎兔龙蛇马羊猴鸡狗猪",
            i = ["小寒", "大寒", "立春", "雨水", "惊蛰", "春分", "清明", "谷雨", "立夏", "小满", "芒种", "夏至", "小暑", "大暑", "立秋", "处暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"],
            j = "日一二三四五六七八九十", k = "初十廿卅",
            l = ["正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "腊"], m = {
                "0101": "*1元旦节",
                "0202": "湿地日",
                "0214": "情人节",
                "0308": "妇女节",
                "0312": "植树节",
                "0315": "消费者权益日",
                "0401": "愚人节",
                "0422": "地球日",
                "0501": "*1劳动节",
                "0504": "青年节",
                "0512": "护士节",
                "0518": "博物馆日",
                "0520": "母亲节",
                "0601": "儿童节",
                "0623": "奥林匹克日",
                "0630": "父亲节",
                "0701": "建党节",
                "0801": "建军节",
                "0903": "抗战胜利日",
                "0910": "教师节",
                1001: "*3国庆节",
                1201: "艾滋病日",
                1224: "平安夜",
                1225: "圣诞节"
            }, n = {
                "0100": "除夕",
                "0101": "*2春节",
                "0115": "元宵节",
                "0505": "*1端午节",
                "0707": "七夕节",
                "0715": "中元节",
                "0815": "*1中秋节",
                "0909": "*1重阳节",
                1015: "下元节",
                1208: "腊八节",
                1223: "小年"
            };
        return new o(new Date(a, b, c))
    }

    var regymdzz, gr, regymd, parseInt, $Q, jeDate, searandom, jefix, ymdzArr, elx, doc = document, win = window,
        jet = {};
    return doc = document, regymdzz = "YYYY|MM|DD|hh|mm|ss|zz", gr = /\-/g, regymd = "YYYY|MM|DD|hh|mm|ss|zz".replace("|zz", ""), parseInt = function (a) {
        return window.parseInt(a, 10)
    }, $Q = function (a, b) {
        return b = b || document, a.nodeType ? a : b.querySelector(a)
    }, jeDate = function (a, b) {
        var c = "function" == typeof b ? b() : b;
        return new jeDatePick(a, c)
    }, jeDate.dateVer = "V6.5.0", jeDate.extend = jet.extend = function () {
        var a, b, c, d, e = !1, f = arguments[0], g = 1, h = arguments.length;
        for ("boolean" == typeof f && (e = f, f = arguments[1] || {}, g = 2), "object" != typeof f && "function" != typeof f && (f = {}), h === g && (f = this, --g); h > g; g++) if (null != (a = arguments[g])) for (b in a) c = f[b], d = a[b], f !== d && void 0 !== d && (f[b] = d);
        return f
    }, jeDate.nowDate = function (a, b) {
        return b = b || "YYYY-MM-DD hh:mm:ss", isNaN(a) || (a = {DD: a}), jet.parse(jet.getDateTime(a), b)
    }, jeDate.convert = function (a) {
        var b, c, d, e;
        return a.format = a.format || "YYYY-MM-DD hh:mm:ss", a.addval = a.addval || [], b = jet.reMatch(a.format), c = {}, jet.each(jet.reMatch(a.val), function (a, d) {
            c[b[a]] = parseInt(d)
        }), d = new DateTime(a.addval, c), e = {
            YYYY: d.GetYear(),
            MM: d.GetMonth(),
            DD: d.GetDate(),
            hh: d.GetHours(),
            mm: d.GetMinutes(),
            ss: d.GetSeconds()
        }, e
    }, jeDate.valText = function (a, b) {
        return jet.valText(a, b)
    }, jeDate.timeStampDate = function (a, b) {
        var c, d, e, f, g, h;
        if (b = b || "YYYY-MM-DD hh:mm:ss", c = /^(-)?\d{1,10}$/.test(a) || /^(-)?\d{1,13}$/.test(a), /^[1-9]*[1-9][0-9]*$/.test(a) && c) {
            if (d = parseInt(a), /^(-)?\d{1,10}$/.test(d)) d = 1e3 * d; else if (/^(-)?\d{1,13}$/.test(d)) d = 1e3 * d; else {
                if (!/^(-)?\d{1,14}$/.test(d)) return alert("时间戳格式不正确"), void 0;
                d = 100 * d
            }
            return e = new Date(d), jet.parse({
                YYYY: e.getFullYear(),
                MM: jet.digit(e.getMonth() + 1),
                DD: jet.digit(e.getDate()),
                hh: jet.digit(e.getHours()),
                mm: jet.digit(e.getMinutes()),
                ss: jet.digit(e.getSeconds())
            }, b)
        }
        return f = jet.reMatch(a), g = new Date(f[0], f[1] - 1, f[2], f[3] || 0, f[4] || 0, f[5] || 0), h = Math.round(g.getTime() / 1e3), h
    }, jeDate.getLunar = function (a) {
        var b = jeLunar(a.YYYY, parseInt(a.MM) - 1, a.DD);
        return {
            nM: b.lnongMonth,
            nD: b.lnongDate,
            cY: parseInt(b.solarYear),
            cM: parseInt(b.solarMonth),
            cD: parseInt(b.solarDate),
            cW: b.inWeekDays,
            nW: b.solarWeekDay
        }
    }, jeDate.parse = jet.parse = function (a, b) {
        return b.replace(new RegExp(regymdzz, "g"), function (b) {
            return "zz" == b ? "00" : jet.digit(a[b])
        })
    }, jet.extend(jet, {
        isType: function (a, b) {
            var c = function (a) {
                return a = a.toLowerCase(), a.replace(/\b(\w)|\s(\w)/g, function (a) {
                    return a.toUpperCase()
                })
            };
            return Object.prototype.toString.call(a) == "[object " + c(b) + "]"
        }, each: function (a, b) {
            var d, e = 0, f = a.length, g = void 0 === f || "function" === a;
            if (g) {
                for (d in a) if (b.call(a[d], d, a[d]) === !1) break
            } else for (; f > e && b.call(a[e], e, a[e++]) !== !1;) ;
            return a
        }, on: function (a, b, c) {
            return a.addEventListener ? (a.addEventListener(b, c, !1), !0) : a.attachEvent ? a.attachEvent("on" + b, c) : (a["on" + b] = c, void 0)
        }, isObj: function (a) {
            for (var b in a) return !0;
            return !1
        }, trim: function (a) {
            return a.replace(/(^\s*)|(\s*$)/g, "")
        }, reMatch: function (a) {
            var b = [], c = "", d = /(^\w{4}|\w{2}\B)/g;
            return c = jet.isNum(a) ? a.replace(d, "$1-") : /^[A-Za-z]+$/.test(a) ? a.replace(d, "$1-") : a, jet.each(c.match(/\w+|d+/g), function (a, c) {
                b.push(jet.isNum(c) ? parseInt(c) : c)
            }), b
        }, equals: function (a, b) {
            if (!b) return !1;
            if (a.length != b.length) return !1;
            for (var c = 0, d = a.length; d > c; c++) if (a[c] instanceof Array && b[c] instanceof Array) {
                if (!a[c].equals(b[c])) return !1
            } else if (a[c] != b[c]) return !1;
            return !0
        }, docScroll: function (a) {
            return a = a ? "scrollLeft" : "scrollTop", document.body[a] | document.documentElement[a]
        }, docArea: function (a) {
            return document.documentElement[a ? "clientWidth" : "clientHeight"]
        }, digit: function (a) {
            return 10 > a ? "0" + (0 | a) : a
        }, isNum: function (a) {
            return /^[+-]?\d*\.?\d*$/.test(a) ? !0 : !1
        }, getDaysNum: function (a, b) {
            var c = 31, d = 0 !== a % 100 && 0 === a % 4 || 0 === a % 400;
            switch (parseInt(b)) {
                case 2:
                    c = d ? 29 : 28;
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    c = 30
            }
            return c
        }, getYM: function (a, b, c) {
            var d = new Date(a, b - 1);
            return d.setMonth(b - 1 + c), {y: d.getFullYear(), m: d.getMonth() + 1}
        }, prevMonth: function (a, b, c) {
            return jet.getYM(a, b, 0 - (c || 1))
        }, nextMonth: function (a, b, c) {
            return jet.getYM(a, b, c || 1)
        }, setCss: function (a, b) {
            for (var c in b) a.style[c] = b[c]
        }, html: function (a, b) {
            return "undefined" == typeof b ? a && 1 === a.nodeType ? a.innerHTML : void 0 : "undefined" != typeof b && 1 == b ? a && 1 === a.nodeType ? a.outerHTML : void 0 : a.innerHTML = b
        }, text: function (a, b) {
            var c = document.all ? "innerText" : "textContent";
            return "undefined" == typeof b ? a && 1 === a.nodeType ? a[c] : void 0 : a[c] = b
        }, val: function (a, b) {
            return "undefined" == typeof b ? a && 1 === a.nodeType && "undefined" != typeof a.value ? a.value : void 0 : (b = null == b ? "" : b + "", a.value = b, void 0)
        }, attr: function (a, b) {
            return a.getAttribute(b)
        }, hasClass: function (a, b) {
            return a.className.match(new RegExp("(\\s|^)" + b + "(\\s|$)"))
        }, stopPropagation: function (a) {
            a && a.stopPropagation ? a.stopPropagation() : window.event.cancelBubble = !0
        }, template: function (a, b) {
            var c = /[^\w\-\.:]/.test(a) ? a : document.getElementById(a).innerHTML, e = function (a) {
                var c, b = "";
                for (c in a) b += "var " + c + '= $D["' + c + '"];';
                return b
            }, f = function (a, b) {
                var c = "var $out='" + a.replace(/[\r\n]/g, "").replace(/^(.+?)\{\%|\%\}(.+?)\{\%|\%\}(.+?)$/g, function (a) {
                        return a.replace(/(['"])/g, "\\$1")
                    }).replace(/\{\%\s*=\s*(.+?)\%\}/g, "';$out+=$1;$out+='").replace(/\{\%(.+?)\%\}/g, "';$1;$out+='") + "';return new String($out);",
                    d = e(b), f = new Function("$D", d + c);
                return new f(b) + ""
            };
            return f(c, b)
        }, isValDiv: function (a) {
            return /textarea|input/.test(a.tagName.toLocaleLowerCase())
        }, valText: function (a, b) {
            var c = $Q(a), d = jet.isValDiv(c) ? "val" : "text";
            return void 0 == b ? jet[d](c) : (jet[d](c, b), void 0)
        }, isBool: function (a) {
            return void 0 == a || 1 == a ? !0 : !1
        }, getDateTime: function (a) {
            var e, b = new DateTime, c = jet.extend({YYYY: null, MM: null, DD: null, hh: 0, mm: 0, ss: 0}, a),
                d = {YYYY: "FullYear", MM: "Month", DD: "Date", hh: "Hours", mm: "Minutes", ss: "Seconds"};
            return jet.each(["ss", "mm", "hh", "DD", "MM", "YYYY"], function (a, e) {
                if (!jet.isNum(parseInt(c[e]))) return null;
                var f = b.GetValue();
                (parseInt(c[e]) || 0 == parseInt(c[e])) && f["set" + d[e]](b["Get" + d[e]]() + ("MM" == e ? -1 : 0) + parseInt(c[e]))
            }), e = {
                YYYY: b.GetYear(),
                MM: b.GetMonth(),
                DD: b.GetDate(),
                hh: b.GetHours(),
                mm: b.GetMinutes(),
                ss: b.GetSeconds()
            }
        }
    }), searandom = function () {
        var c, a = "", b = [1, 2, 3, 4, 5, 6, 7, 8, 9, 0];
        for (c = 0; 8 > c; c++) a += b[Math.round(Math.random() * (b.length - 1))];
        return a
    }, jefix = "jefixed", ymdzArr = jet.reMatch(regymdzz), elx = "#jedate", jet.extend(jeDatePick.prototype, {
        init: function () {
            var g, l, m, n, a = this, b = a.$opts, e = (new Date, b.trigger), f = b.initDate || [], h = b.range,
                j = (void 0 == b.zIndex ? 1e4 : b.zIndex, jet.isBool(b.isShow)),
                k = void 0 == b.isinitVal || 0 == b.isinitVal ? !1 : !0;
            a.setDatas(), b.before && b.before(a.valCell), k && e && j && (f[1] ? (l = jet.getDateTime(f[0]), g = [{
                YYYY: l.YYYY,
                MM: jet.digit(l.MM),
                DD: jet.digit(l.DD),
                hh: jet.digit(l.hh),
                mm: jet.digit(l.mm),
                ss: jet.digit(l.ss)
            }]) : g = a.getValue(jet.isObj(f[0]) ? f[0] : {}), h || a.setValue([g[0]], b.format, !0)), m = function () {
                var f, g, i, b = jet.reMatch(a.format), c = "" != a.getValue(), d = [],
                    e = 7 == a.dlen ? "hh:mm:ss" : "YYYY-MM" + (a.dlen <= 2 ? "" : "-DD");
                return a.selectValue = [jet.parse(jet.getDateTime({}), e)], c && j ? (f = a.getValue().split(h), jet.each(new Array(h ? 2 : 1), function (a) {
                    d[a] = {}, jet.each(jet.reMatch(f[a]), function (c, e) {
                        d[a][b[c]] = parseInt(e)
                    })
                }), h && (a.selectValue = f)) : (g = a.getValue({})[0], i = jet.nextMonth(g.YYYY, g.MM || jet.getDateTime({}).MM), a.dlen > 2 && a.dlen <= 6 ? {
                    YYYY: i.y,
                    MM: i.m
                } : {}, d = [g]), a.selectDate = d, d
            }, n = [], a.minDate = "", a.maxDate = "", j && e || (n = m()), j && e ? e && jet.on(a.valCell, e, function () {
                if (!(document.querySelectorAll(elx).length > 0)) {
                    var c = m();
                    a.minDate = jet.isType(b.minDate, "function") ? b.minDate(a) : b.minDate, a.maxDate = jet.isType(b.maxDate, "function") ? b.maxDate(a) : b.maxDate, a.storeData(c[0], c[1]), a.renderDate()
                }
            }) : (a.minDate = jet.isType(b.minDate, "function") ? b.minDate(a) : b.minDate, a.maxDate = jet.isType(b.maxDate, "function") ? b.maxDate(a) : b.maxDate, a.storeData(n[0], n[1]), a.renderDate(), b.succeed && b.succeed(a.dateCell))
        }, setDatas: function () {
            var a = this, b = a.$opts, c = b.range, d = [], e = jet.isBool(b.isShow), f = b.multiPane;
            a.$data = jet.extend({year: !1, month: !1, day: !0, time: !1, timebtn: !1}, {
                shortcut: [],
                lang: b.language,
                yaerlist: [],
                monthlist: [[], []],
                ymlist: [[], []],
                daylist: [[], []],
                clear: b.isClear,
                today: c ? !1 : b.isToday,
                yes: b.isYes,
                pane: f ? 1 : 2
            }), b.shortcut.length > 0 && (jet.each(b.shortcut, function (a, b) {
                var f, c = [], e = jet.isType(b.val, "function") ? b.val() : b.val;
                if (jet.isType(e, "object")) {
                    for (f in e) c.push(f + ":" + e[f]);
                    d.push(jet.extend({}, {name: b.name, val: "{" + c.join("#") + "}"}))
                }
            }), a.$data.shortcut = d), a.dlen = function () {
                var d, e, b = jet.reMatch(a.format), c = [];
                return jet.each(ymdzArr, function (a, d) {
                    jet.each(b, function (a, b) {
                        d == b && c.push(b)
                    })
                }), d = c.length, e = "hh" == c[0] && 3 >= d ? 7 : d, e
            }(), a.$data.dlen = a.dlen, a.timeInspect = !1, 1 == a.dlen ? jet.extend(a.$data, {
                year: !0,
                day: !1
            }) : 2 == a.dlen ? jet.extend(a.$data, {
                month: !0,
                day: !1
            }) : a.dlen > 3 && a.dlen <= 6 ? a.$data.timebtn = !0 : 7 == a.dlen && jet.extend(a.$data, {
                day: !1,
                time: !0
            }), e || (a.$data.clear = !1, a.$data.yes = !1)
        }, renderDate: function () {
            var f, g, h, i, j, k, a = this, b = a.$opts, c = jet.isBool(b.isShow), d = c ? elx : elx + searandom(),
                e = {zIndex: void 0 == b.zIndex ? 1e4 : b.zIndex};
            void 0 == a.dateCell && (a.dateCell = document.createElement("div"), a.dateCell.id = d.replace(/\#/g, ""), a.dateCell.className = elx.replace(/\#/g, "") + " " + (b.shortcut.length > 0 ? " leftmenu" : ""), a.dateCell.setAttribute("author", "chen guojun")), jet.html(a.dateCell, jet.template(a.dateTemplate(), a.$data)), jet.isObj(b.theme) && (f = document.createElement("style"), g = ".jedate" + searandom(), h = b.theme, i = "background-color:" + h.bgcolor, j = "color:" + (void 0 == h.color ? "#FFFFFF" : h.color), k = void 0 == h.pnColor ? "" : "color:" + h.pnColor + ";", a.dateCell.className = a.dateCell.className + " " + g.replace(/^./g, ""), f.setAttribute("type", "text/css"), f.innerHTML = g + " .jedate-menu p:hover{" + i + ";" + j + ";}" + g + " .jedate-header em{" + j + ";}" + g + " .jedate-content .yeartable td.action span," + g + " .jedate-content .monthtable td.action span," + g + " .jedate-content .yeartable td.action span:hover," + g + " .jedate-content .monthtable td.action span:hover{" + i + ";border:1px " + h.bgcolor + " solid;" + j + ";}" + g + " .jedate-content .daystable td.action," + g + " .jedate-content .daystable td.action:hover," + g + " .jedate-content .daystable td.action .lunar," + g + " .jedate-header," + g + " .jedate-time .timeheader," + g + " .jedate-time .hmslist ul li.action," + g + " .jedate-time .hmslist ul li.action:hover," + g + " .jedate-time .hmslist ul li.disabled.action," + g + " .jedate-footbtn .timecon," + g + " .jedate-footbtn .btnscon span{" + i + ";" + j + ";}" + g + " .jedate-content .daystable td.other," + g + " .jedate-content .daystable td.other .nolunar," + g + " .jedate-content .daystable td.other .lunar{" + k + "}" + g + " .jedate-content .daystable td.contain," + g + " .jedate-content .daystable td.contain:hover{background-" + k + "}", a.dateCell.appendChild(f)), a.compileBindNode(a.dateCell), document.querySelectorAll(d).length > 0 && document.body.removeChild($Q(d)), c ? document.body.appendChild(a.dateCell) : a.valCell.appendChild(a.dateCell), jet.setCss(a.dateCell, jet.extend({position: c ? 1 == b.fixed ? "absolute" : "fixed" : "relative"}, c ? e : {})), a.methodEventBind(), (7 == a.dlen || a.dlen > 3 && a.dlen <= 6) && a.locateScroll(), b.festival && "cn" == b.language.name && a.showFestival(), c && (a.dateOrien(a.dateCell, a.valCell), a.blankArea())
        }, setValue: function (a, b, c) {
            var f, g, h, i, d = this, e = d.valCell;
            return b = b || d.format, "string" == typeof a && "" != a ? (g = a.split(d.$opts.range), h = [], jet.each(g, function (a, c) {
                var d = jet.reMatch(c), e = {};
                jet.each(jet.reMatch(b), function (a, b) {
                    e[b] = d[a]
                }), h.push(e)
            }), f = h) : f = a, i = d.parseValue(f, b), 0 != c && jet.valText(e, i), i
        }, getValue: function (a) {
            var e, m, n, o, p, q, r, s, t, u, v, b = this, c = b.valCell, d = b.$opts, f = (new DateTime).reDate(),
                g = f.GetYear(), h = f.GetMonth(), i = f.GetDate(), j = f.GetHours(), k = f.GetMinutes(),
                l = f.GetSeconds();
            return void 0 == a && jet.isBool(d.isShow) ? e = jet.valText(c) : (m = jet.isBool(d.isShow) ? "" == jet.valText(c) : !jet.isBool(d.isShow), n = jet.extend({
                YYYY: null,
                MM: null,
                DD: null
            }, a || {}), o = [], p = new Array(2), q = function (a) {
                return [void 0 == n[a] || null == n[a], n[a]]
            }, r = [{YYYY: g, MM: h, DD: i, hh: j, mm: k, ss: l, zz: 0}, {
                YYYY: g,
                MM: h,
                DD: i,
                hh: j,
                mm: k,
                ss: l,
                zz: 0
            }], m ? jet.each(p, function (a) {
                var b = {};
                jet.each(ymdzArr, function (c, d) {
                    b[d] = parseInt(q(d)[0] ? r[a][d] : q(d)[1])
                }), o.push(jet.extend(r[a], b))
            }) : (s = 0 != d.range, t = b.getValue(), u = t.split(d.range), v = jet.reMatch(b.format), jet.each(p, function (b) {
                var e, c = {}, d = s ? jet.reMatch(u[b]) : jet.reMatch(t);
                jet.each(v, function (a, b) {
                    c[b] = d[a]
                }), e = jet.extend(c, a || {}), o.push(jet.extend(r[b], e))
            })), e = o), e
        }, storeData: function (a, b) {
            var k, c, d, e, f, g, i, j, l, m, n, o, p, r;
            b = b || {}, c = this, d = c.$opts, e = d.multiPane, f = c.valCell, g = (new Date).getDate(), c.$data, i = jet.isObj(b), j = {
                yearlist: [],
                monthlist: [[], []],
                daylist: [],
                daytit: [],
                timelist: []
            }, l = null == a.DD ? g : a.DD, m = null == b.DD ? g : b.DD, n = {
                hh: a.hh,
                mm: a.mm,
                ss: a.ss
            }, o = {
                hh: b.hh || 0,
                mm: b.mm || 0,
                ss: b.ss || 0
            }, j.yearlist.push(c.eachYear(parseInt(a.YYYY), 1)), 0 == e && (p = i ? b.YYYY : a.YYYY, j.yearlist.push(c.eachYear(parseInt(p), 2))), j.monthlist[0] = c.eachMonth(a.YYYY, 0), 0 == e && (i ? b.YYYY : a.YYYY + 1, j.monthlist[1] = c.eachMonth(a.YYYY + 1, 1)), j.daylist.push(c.eachDays(a.YYYY, a.MM, l, 0)), j.daytit.push({
                YYYY: a.YYYY,
                MM: a.MM
            }), 0 == e && (r = jet.nextMonth(a.YYYY, a.MM), j.daylist.push(c.eachDays(r.y, r.m, m, 1)), j.daytit.push({
                YYYY: r.y,
                MM: r.m
            })), c.selectTime = [n, o], j.timelist.push(c.eachTime(n, 1)), 0 == e && (k = 7 == c.dlen && d.range && !i ? n : o, 7 == c.dlen && d.range && "" == jet.valText(f) && (c.selectTime[1] = jet.extend(o, n)), j.timelist.push(c.eachTime(k, 2))), jet.extend(c.$data, j)
        }, dateTemplate: function () {
            var x, y, a = this, b = a.$opts, c = b.multiPane, d = "", e = "", f = b.language,
                g = "cn" == f.name ? "年" : "", h = "cn" == f.name ? "月" : "", j = function () {
                    var b = [], d = c ? "11" : "23";
                    return 1 == a.dlen ? b = ["{%=yearlist[i][0].y-" + d + "%}", "{%=yearlist[i][yearlist[i].length-1].y%}"] : 2 == a.dlen ? b = c ? ["{%=yearlist[0][0].y-1%}", "{%=yearlist[0][0].y+1%}"] : ["{%=yearlist[i][0].y-" + d + "%}", "{%=yearlist[i][yearlist[i].length-1].y%}"] : a.dlen > 2 && a.dlen <= 6 && (b = ["{%=yearlist[0][0].y-1%}", "{%=yearlist[0][0].y+1%}"]), b
                }(), k = '<em class="yearprev yprev jedatefont" @on="yearBtn(lprev,' + j[0] + ')">&#xed6c2;</em>',
                n = ('<em class="yearnext ynext jedatefont" on="yearBtn(lnext,' + j[2] + ')">&#xed6c5;</em>', '<em class="yearprev yprev jedatefont" on="yearBtn(rprev,' + j[3] + ')">&#xed6c2;</em>', '<em class="yearnext ynext jedatefont" @on="yearBtn(rnext,' + j[1] + ')">&#xed6c5;</em>'),
                o = '{% if(dlen>2){ %}<em class="monthprev mprev jedatefont" @on="monthBtn(mprev,{%=daytit[i].YYYY%}-{%=daytit[i].MM%})">&#xed602;</em>{% } %}',
                p = '{% if(dlen>2){ %}<em class="monthnext mnext jedatefont" @on="monthBtn(mnext,{%=daytit[i].YYYY%}-{%=daytit[i].MM%})">&#xed605;</em>{% } %}',
                q = '<table class="yeartable year{%= i==0 ? "left":"right"%}" style="display:{%=year ? "block":"none"%};"><tbody><tr>{% for(var y=0;y<=11;y++){ %}<td class="{%=yearlist[i][y].style%}" @on="yearClick({%=yearlist[i][y].y%})"><span>{%=yearlist[i][y].y%}' + g + "</span></td>{% if((y+1)%3==0){ %} </tr>{% } %} {% } %} </tbody></table>",
                r = '<table class="monthtable month{%= i==0 ? "left":"right"%}" style="display:{%=month ? "block":"none"%};"><tbody><tr>{% for(var m=0;m<=11;m++){ %}<td class="{%=monthlist[i][m].style%}" ym="{%=monthlist[i][m].y%}-{%=monthlist[i][m].m%}" @on="monthClick({%=monthlist[i][m].y%}-{%=monthlist[i][m].m%})"><span>{%=monthlist[i][m].m%}' + h + "</span></td>{% if((m+1)%3==0){ %} </tr>{% } %} {% } %} </tbody></table>",
                s = '<table class="daystable days{%= i==0 ? "left":"right"%}" style="display:{%=day ? "block":"none"%};"><thead><tr>{% for(var w=0;w<lang.weeks.length;w++){ %} <th>{%=lang.weeks[w]%}</th> {% } %}</tr></thead><tbody><tr>{% for(var d=0;d<=41;d++){ %}<td class="{%=daylist[i][d].style%}" ymd="{%=daylist[i][d].ymd%}" @on="daysClick({%=daylist[i][d].ymd%})">{%=daylist[i][d].day%}</td>{% if((d+1)%7==0){ %} </tr>{% } %} {% } %} </tbody></table>',
                t = '<div class="jedate-time">{% for(var h=0;h<timelist.length;h++){ %}<div class="timepane"><div class="timeheader">{%= timelist.length == 1 ? lang.timetxt[0]:lang.timetxt[h+1]%}</div><div class="timecontent"><div class="hmstitle"><p>{%=lang.times[0]%}</p><p>{%=lang.times[1]%}</p><p>{%=lang.times[2]%}</p></div><div class="hmslist">{% for(var t=0;t<3;t++){ %}<div class="hmsauto"><ul>{% for(var s=0;s<timelist[h][t].length;s++){ %}<li class="{%=timelist[h][t][s].style%}" @on="hmsClick({%= h %},{%= h>0?3+t:t %})">{%= timelist[h][t][s].hms < 10 ? "0" + timelist[h][t][s].hms :timelist[h][t][s].hms %}</li>{% } %}</ul></div>{% } %}</div></div></div>{% } %}</div>',
                u = b.shortcut.length > 0 ? "{% for(var s=0;s<shortcut.length;s++){ %}<p @on=shortClick({%= shortcut[s].val %})>{%=shortcut[s].name%}</p>{% } %}" : "",
                v = function () {
                    var b = "";
                    return 1 == a.dlen ? b = '<span class="ymbtn">{%=yearlist[i][0].y%}' + g + " ~ {%=yearlist[i][yearlist[i].length-1].y%}" + g + "</span>" : 2 == a.dlen ? b = '<span class="ymbtn" @on="yearShow({%=yearlist[0][i].y%})">{%=yearlist[0][i].y%}' + g + "</span>" : a.dlen > 2 && a.dlen <= 6 && (b = '<span class="ymbtn" @on="monthShow({%=daytit[i].MM%})">{%=daytit[i].MM%}' + h + "</span>" + '<span class="ymbtn" @on="yearShow({%=daytit[i].YYYY%})">{%=daytit[i].YYYY%}' + g + "</span>"), b
                }(), w = function () {
                    var b = "";
                    return 1 == a.dlen ? b = c ? [k + n] : [k, n] : 2 == a.dlen ? b = c ? [k + n] : [k, n] : a.dlen > 2 && a.dlen <= 6 ? b = c ? [k + o + p + n] : [k + o, p + n] : 7 == a.dlen && (b = ""), b
                }();
            return 1 == a.dlen ? d = q : 2 == a.dlen ? d = q + r : 3 == a.dlen ? d = q + r + s : a.dlen > 3 && a.dlen <= 6 ? (d = q + r + s, e = t) : 7 == a.dlen && (e = t), x = '{% for(var i=0;i<pane;i++){ %}<div class="jedate-pane"><div class="jedate-header">{% if(i==0){ %}' + w[0] + "{% }else{ %}" + w[1] + "{% } %}" + v + "</div>" + '<div class="jedate-content{%= i==1?" bordge":"" %}">' + d + "</div>" + "</div>{% } %}", y = '{% if(timebtn){%}<div class="timecon" style="cursor: pointer;" @on="timeBtn">{%=lang.timetxt[0]%}</div>{% } %}<div class="btnscon">{% if(clear){ %}<span class="clear" @on="clearBtn">{%=lang.clear%}</span>{% } %}{% if(today){ %}<span class="today" @on="nowBtn">{%=lang.today%}</span>{% } %}{% if(yes){ %}<span class="setok" @on="sureBtn">{%=lang.yes%}</span>{% } %}</div>', '<div class="jedate-menu" style="display:{%=shortcut.length>0 ? "block":"none"%};">' + u + '</div><div class="jedate-wrap">' + x + "</div>" + e + '<div class="jedate-footbtn">' + y + '</div><div class="jedate-tips"></div>'
        }, compileBindNode: function (a) {
            var b = this, c = "@on", d = function (a) {
                var b = /\(.*\)/.exec(a);
                return b ? (b = b[0], a = a.replace(b, ""), b = b.replace(/[\(\)\'\"]/g, "").split(",")) : b = [], [a, b]
            };
            jet.each(a.childNodes, function (a, e) {
                var f, g;
                1 === e.nodeType && (b.$opts.festival || e.removeAttribute("ymd"), b.compileBindNode(e), f = e.getAttribute(c), null != f && (g = d(f), jet.on(e, "click", function () {
                    b[g[0]] && b[g[0]].apply(e, g[1])
                }), e.removeAttribute(c)))
            })
        }, methodEventBind: function () {
            var that = this, opts = that.$opts, multi = opts.multiPane, DTS = that.$data,
                result = (new DateTime).reDate(), dateY = result.GetYear(), dateM = result.GetMonth(),
                dateD = result.GetDate(), range = opts.range, elCell = that.dateCell;
            jet.extend(that, {
                yearBtn: function (a, b) {
                    var f, c = b.split("#"), e = (jet.reMatch(c[0]), that.selectTime);
                    exarr = [jet.extend({
                        YYYY: parseInt(b),
                        MM: dateM,
                        DD: dateD
                    }, e[0]), {}], f = that.parseValue([exarr[0]], that.format), that.storeData(exarr[0], exarr[1]), that.renderDate(), opts.toggle && opts.toggle({
                        elem: that.valCell,
                        val: f,
                        date: exarr[0]
                    })
                }, yearShow: function () {
                    if (DTS.year = DTS.year ? !1 : !0, DTS.month = that.dlen < 3 ? !0 : !1, that.dlen > 2 && that.dlen <= 6) {
                        var b = $Q(".daystable", elCell);
                        DTS.day = "none" == b.style.display ? !0 : !1
                    }
                    that.renderDate()
                }, monthBtn: function (a, b) {
                    var f, g, j, k, c = jet.reMatch(b), d = that.selectTime, e = [], h = parseInt(c[0]),
                        i = parseInt(c[1]);
                    range ? ("mprev" == a ? (f = jet.prevMonth(h, i), g = jet.nextMonth(f.y, f.m)) : (g = jet.nextMonth(h, i), f = jet.prevMonth(g.y, g.m)), e = [jet.extend({
                        YYYY: f.y,
                        MM: f.m,
                        DD: dateD
                    }, d[0]), {
                        YYYY: g.y,
                        MM: g.m,
                        DD: dateD
                    }]) : (j = "mprev" == a ? jet.prevMonth(h, i) : jet.nextMonth(h, i), e = [jet.extend({
                        YYYY: j.y,
                        MM: j.m,
                        DD: dateD
                    }, d[0]), {}]), k = that.parseValue([e[0]], that.format), that.storeData(e[0], e[1]), that.renderDate(), opts.toggle && opts.toggle({
                        elem: that.valCell,
                        val: k,
                        date: e[0]
                    })
                }, monthShow: function () {
                    if (DTS.year = !1, DTS.month = DTS.month ? !1 : !0, that.dlen > 2 && that.dlen <= 6) {
                        var b = $Q(".daystable", elCell);
                        DTS.day = "none" == b.style.display ? !0 : !1
                    }
                    that.renderDate()
                }, shortClick: function (val) {
                    var nYM, ymarr, reval = val.replace(/\#/g, ","), evobj = eval("(" + reval + ")"),
                        gval = jet.getDateTime(evobj), tmval = that.selectTime;
                    that.selectValue = [jet.parse(gval, "YYYY-MM-DD")], that.selectDate = [{
                        YYYY: gval.YYYY,
                        MM: gval.MM,
                        DD: gval.DD
                    }], opts.onClose ? (nYM = jet.nextMonth(gval.YYYY, gval.MM), ymarr = [{
                        YYYY: gval.YYYY,
                        MM: gval.MM,
                        DD: gval.DD
                    }, {
                        YYYY: nYM.y,
                        MM: nYM.m,
                        DD: null
                    }], that.storeData(jet.extend(ymarr[0], tmval[0]), jet.extend(ymarr[1], tmval[1])), that.renderDate()) : (that.setValue(gval, that.format), that.closeDate())
                }, yearClick: function (a) {
                    var b, c, d, e, f, g;
                    jet.hasClass(this, "disabled") || (b = "", c = that.dlen, range && 1 == c ? (d = that.selectValue.length, that.selectDate = 2 == d ? [{
                        YYYY: parseInt(a),
                        MM: dateM
                    }] : [{YYYY: that.selectDate[0].YYYY, MM: that.selectDate[0].MM}, {
                        YYYY: parseInt(a),
                        MM: dateM
                    }], that.selectValue = 2 == d ? [a + "-" + jet.digit(dateM)] : [that.selectValue[0], a + "-" + jet.digit(dateM)], 2 == that.selectValue.length && (e = [that.selectValue[0], that.selectValue[1]], f = [{}, {}], e.sort(function (a, b) {
                        return a > b ? 1 : -1
                    }), that.selectValue = e, jet.each(e, function (a, b) {
                        jet.each(jet.reMatch(b), function (b, c) {
                            f[a][ymdzArr[b]] = c
                        })
                    }), that.selectDate = f)) : c > 1 && 6 >= c ? b = parseInt(a) : (that.selectValue = [a + "-" + jet.digit(dateM)], that.selectDate = [{
                        YYYY: parseInt(a),
                        MM: dateM
                    }]), DTS.year = 1 == c ? !0 : !1, DTS.month = 3 > c ? !0 : !1, DTS.day = c > 2 && 6 >= c ? !0 : !1, g = c > 1 && 6 >= c ? b : parseInt(that.selectDate[0].YYYY), that.storeData(jet.extend({
                        YYYY: g,
                        MM: dateM,
                        DD: dateD
                    }, that.selectTime[0]), {}), that.renderDate())
                }, monthClick: function (a) {
                    var b, c, d, e;
                    jet.hasClass(this, "disabled") || (b = jet.reMatch(a), c = [{}, {}], d = that.selectValue.length, range ? (that.selectDate = 2 == d ? [{
                        YYYY: b[0],
                        MM: b[1]
                    }] : [{YYYY: that.selectDate[0].YYYY, MM: that.selectDate[0].MM}, {
                        YYYY: parseInt(a),
                        MM: b[1]
                    }], that.selectValue = 2 == d ? [a] : [that.selectValue[0], a], 2 == that.selectValue.length && (e = [that.selectValue[0], that.selectValue[1]], e.sort(function (a, b) {
                        return a > b ? 1 : -1
                    }), that.selectValue = e, jet.each(e, function (a, b) {
                        jet.each(jet.reMatch(b), function (b, d) {
                            c[a][ymdzArr[b]] = d
                        })
                    }), that.selectDate = c)) : (that.selectValue = [a], that.selectDate = [{
                        YYYY: b[0],
                        MM: b[1]
                    }]), that.dlen > 2 && (DTS.year = !1, DTS.month = !1), DTS.day = that.dlen > 2 && that.dlen <= 6 ? !0 : !1, that.storeData(jet.extend({
                        YYYY: parseInt(that.selectDate[0].YYYY),
                        MM: parseInt(that.selectDate[0].MM),
                        DD: dateD
                    }, that.selectTime[0]), {}), that.renderDate())
                }, daysClick: function (a) {
                    var h, i, b, c, d, e, f, j;
                    jet.hasClass(this, "disabled") || (b = that.selectTime, c = jet.reMatch(a), d = that.selectValue.length, e = "", f = [{}, {}], range ? (1 == d ? (j = [that.selectValue[0], a], j.sort(function (a, b) {
                        return a > b ? 1 : -1
                    }), that.selectValue = j, jet.each(j, function (a, b) {
                        jet.each(jet.reMatch(b), function (b, c) {
                            f[a][ymdzArr[b]] = c
                        })
                    }), that.selectDate = f) : (that.selectValue = [a], f = [{
                        YYYY: c[0],
                        MM: c[1],
                        DD: c[2]
                    }], that.selectDate = [{
                        YYYY: c[0],
                        MM: c[1],
                        DD: c[2]
                    }, {}]), h = jet.nextMonth(f[0].YYYY, f[0].MM), i = [{
                        YYYY: f[0].YYYY,
                        MM: f[0].MM,
                        DD: f[0].DD
                    }, {
                        YYYY: h.y,
                        MM: h.m,
                        DD: null
                    }], that.storeData(jet.extend(i[0], b[0]), jet.extend(i[1], b[1])), that.renderDate()) : (that.selectValue = [a], that.selectDate = [{
                        YYYY: c[0],
                        MM: c[1],
                        DD: c[2]
                    }, {YYYY: c[0], MM: c[1], DD: c[2]}], jet.each(new Array(0 == range ? 1 : 2), function (a) {
                        jet.each(c, function (b, c) {
                            f[a][ymdzArr[b]] = c
                        }), jet.extend(f[a], b[a])
                    }), opts.onClose ? (that.storeData(jet.extend(f[0], b[0]), jet.extend(f[1], b[1])), that.renderDate()) : (e = that.setValue(f, that.format), that.closeDate(), opts.donefun && opts.donefun.call(that, {
                        elem: that.valCell,
                        val: e,
                        date: f
                    }))))
                }, hmsClick: function (a, b) {
                    var j, k, l, m, c = parseInt(b), d = parseInt(jet.text(this)), e = parseInt(a), f = "action",
                        g = ["hh", "mm", "ss"], h = $Q(".jedate-time", that.dateCell).querySelectorAll("ul")[c],
                        i = that.$data.timelist[0].length;
                    jet.hasClass(this, "disabled") || (jet.each(h.childNodes, function (a, b) {
                        var c = new RegExp("(^|\\s+)" + f + "(\\s+|$)", "g");
                        b.className = c.test(b.className) ? b.className.replace(c, "") : b.className
                    }), that.selectTime[e][1 == e ? g[c - i] : g[c]] = d, this.className = this.className + f, j = h.querySelector("." + f), h.scrollTop = j ? j.offsetTop - 145 : 0, 7 == that.dlen && 0 == a && range && !multi && (k = that.getValue({}), l = jet.nextMonth(k[0].YYYY, k[0].MM), m = that.selectTime, that.storeData({
                        YYYY: k[0].YYYY,
                        MM: k[0].MM,
                        DD: null,
                        hh: m[0].hh,
                        mm: m[0].mm,
                        ss: m[0].ss
                    }, {YYYY: l.y, MM: l.m, DD: null, hh: m[1].hh, mm: m[1].mm, ss: m[1].ss}), that.renderDate()))
                }, timeBtn: function () {
                    var a = $Q(".jedate-time", elCell), b = "none" == a.style.display;
                    jet.text(this, b ? opts.language.backtxt : opts.language.timetxt[0]), jet.setCss(a, {display: b ? "block" : "none"})
                }, clearBtn: function () {
                    jet.valText(that.valCell, ""), that.selectDate = [jet.parse(jet.getDateTime({}), "YYYY-MM-DD hh:mm:ss")], that.closeDate(), opts.clearfun && opts.clearfun.call(that)
                }, nowBtn: function () {
                    var c, a = jet.getDateTime({}), b = jet.nextMonth(a.YYYY, a.MM);
                    that.selectDate = [a], c = opts.isShow ? that.setValue([a], that.format, !0) : jet.parse(a, that.format), opts.onClose && range || !opts.isShow ? (that.storeData(a, {
                        YYYY: b.y,
                        MM: b.m,
                        DD: null,
                        hh: 0,
                        mm: 0,
                        ss: 0
                    }), that.renderDate()) : that.closeDate(), opts.donefun && opts.donefun.call(that, {
                        elem: that.valCell,
                        val: c,
                        date: a
                    })
                }, sureBtn: function () {
                    var e, f, g, h, a = that.selectValue.length > 1 ? [{}, {}] : [{}], b = "", c = that.selectTime,
                        d = function (a) {
                            var b = void 0 == a.hh ? 0 : a.hh, c = void 0 == a.mm ? 0 : a.mm,
                                d = void 0 == a.ss ? 0 : a.ss;
                            return parseInt(jet.digit(b) + "" + jet.digit(c) + jet.digit(d))
                        };
                    if (range) {
                        if (that.selectValue.length > 1 ? (e = that.selectValue, e.sort(function (a, b) {
                            return a > b ? 1 : -1
                        }), jet.each(e, function (b, d) {
                            jet.each(jet.reMatch(d), function (c, d) {
                                a[b][ymdzArr[c]] = d
                            }), jet.extend(a[b], c[b])
                        })) : 7 == that.dlen && c.length > 1 && (a = c), d(c[0]) >= d(c[1]), g = that.selectValue, "", void 0 != g[1] && (g[0].replace(/\-/g, "") == g[1].replace(/\-/g, "")), 1 == g.length && that.dlen < 7) return that.tips("cn" == opts.language.name ? "未选结束日期" : "Please select the end date"), void 0;
                        // if (7 == that.dlen && f || h && f) return that.tips("cn" == opts.language.name ? "结束时间必须大于开始时间" : "The end time must be greater than the start time"), void 0
                    } else jet.each(new Array(0 == range ? 1 : 2), function (b) {
                        7 != that.dlen && jet.each(jet.reMatch(that.selectValue[0]), function (c, d) {
                            a[b][ymdzArr[c]] = d
                        }), jet.extend(a[b], c[b])
                    });
                    b = that.setValue(a, that.format, opts.isShow ? !0 : !1), opts.isShow && that.closeDate(), opts.donefun && opts.donefun.call(that, {
                        elem: that.valCell,
                        val: b,
                        date: a
                    })
                }, blankArea: function () {
                    jet.on(document, "mouseup", function (a) {
                        jet.stopPropagation(a), that.closeDate()
                    }), jet.on($Q(elx), "mouseup", function (a) {
                        jet.stopPropagation(a)
                    })
                }
            })
        }, eachYear: function (a, b) {
            var l, c = this, e = (c.$opts, parseInt(a)), f = [], g = "", h = c.selectDate, j = jet.reMatch(c.minDate),
                k = jet.reMatch(c.maxDate), i = 1 == b ? e : c.yindex;
            for (c.yindex = 1 == b ? 12 + e : 12 + c.yindex, l = void 0 == h[1] ? "" : h[1].YYYY; i < c.yindex; i++) g = i == h[0].YYYY || i == l ? "action" : i > h[0].YYYY && l > i ? "contain" : i < j[0] || i > k[0] ? "disabled" : "", f.push({
                style: g,
                y: i
            });
            return f
        }, eachMonth: function (a) {
            var c = this, d = c.$opts, f = (d.range, []), g = c.selectDate, h = "", i = d.language.month,
                j = jet.reMatch(c.minDate), k = jet.reMatch(c.maxDate), l = parseInt(j[0] + "" + jet.digit(j[1])),
                m = parseInt(k[0] + "" + jet.digit(k[1])), n = parseInt(g[0].YYYY + "" + jet.digit(g[0].MM)),
                o = g[1] ? parseInt(g[1].YYYY + "" + jet.digit(g[1].MM)) : 0;
            return jet.each(i, function (b, c) {
                var d = parseInt(a + "" + jet.digit(c));
                h = d == n || d == o ? "action" : d > n && o > d ? "contain" : l > d || d > m ? "disabled" : "", f.push({
                    style: h,
                    y: a,
                    m: c
                })
            }), f
        }, eachDays: function (a, b) {
            var F, G, H, I, J, K, L, M, N, O, P, Q, R, e = this, f = 0, g = [], h = e.$opts,
                j = (jet.isBool(h.multiPane), new Date(a, b - 1, 1).getDay() || 7),
                l = (0 != h.range, jet.getDaysNum(a, b)), n = e.selectDate, o = jet.prevMonth(a, b),
                q = (jet.isBool(h.isShow), jet.getDaysNum(a, o.m)), r = jet.nextMonth(a, b),
                t = (e.valCell, h.language), u = h.valiDate || [], v = jet.reMatch(e.minDate),
                w = parseInt(v[0] + "" + jet.digit(v[1]) + jet.digit(v[2])), x = jet.reMatch(e.maxDate),
                y = parseInt(x[0] + "" + jet.digit(x[1]) + jet.digit(x[2])),
                z = n[0] ? parseInt(n[0].YYYY + "" + jet.digit(n[0].MM) + jet.digit(n[0].DD)) : "",
                A = n[1] ? parseInt(n[1].YYYY + "" + jet.digit(n[1].MM) + jet.digit(n[1].DD)) : "",
                B = function (a, b, c) {
                    var d = h.marks, e = function (a, b) {
                        for (var c = a.length; c--;) if (a[c] === b) return !0;
                        return !1
                    }, f = jet.isType(d, "array");
                    return f && d.length > 0 && e(d, a + "-" + jet.digit(b) + "-" + jet.digit(c)) ? '<i class="marks"></i>' : ""
                }, C = function (a, b, c) {
                    var e, f, g, d = "";
                    return 1 == h.festival && "cn" == t.name ? (e = jeLunar(a, b - 1, c), f = e.solarFestival || e.lunarFestival, g = "" != (f && e.jieqi) ? f : e.jieqi || e.showInLunar, d = '<p><span class="solar">' + c + '</span><span class="lunar">' + g + "</span></p>") : d = '<p class="nolunar">' + c + "</p>", d
                }, D = function (a, b, c, d) {
                    var e = parseInt(a + "" + jet.digit(b) + jet.digit(c));
                    if (d) {
                        if (e >= w && y >= e) return !0
                    } else if (w > e || e > y) return !0
                }, E = function (a, b) {
                    var d, f, g, h, i, c = function (a, b) {
                        for (var c in b) if (b[c] == a) return !0;
                        return !1
                    };
                    return u.length > 0 && "" != u[0] && (/\%/g.test(u[0]) ? (d = u[0].replace(/\%/g, "").split(","), f = [], jet.each(d, function (a, b) {
                        f.push(jet.digit(parseInt(b)))
                    }), g = 0 == c(jet.digit(a), f), b = jet.isBool(u[1]) ? g ? " disabled" : b : g ? b : " disabled") : (h = e.dateRegExp(u[0]), i = h.test(jet.digit(a)), b = jet.isBool(u[1]) ? i ? " disabled" : b : i ? b : " disabled")), b
                };
            for (F = q - j + 1; q >= F; F++, f++) G = B(o.y, o.m, F), H = D(o.y, o.m, F, !1) ? "disabled" : "other", H = E(F, H), g.push({
                style: H,
                ymd: o.y + "-" + jet.digit(o.m) + "-" + jet.digit(F),
                day: C(o.y, o.m, F) + G
            });
            for (I = 1; l >= I; I++, f++) J = B(a, b, I), K = "", L = parseInt(a + "" + jet.digit(b) + jet.digit(I)), M = L > z, N = A > L, K = D(a, b, I, !0) ? L == z || L == A ? " action" : M && N ? " contain" : "" : " disabled", K = E(I, K), g.push({
                style: "normal" + K,
                ymd: a + "-" + jet.digit(b) + "-" + jet.digit(I),
                day: C(a, b, I) + J
            });
            for (O = 1, P = 42 - f; P >= O; O++) Q = B(r.y, r.m, O), R = D(r.y, r.m, O, !1) ? "disabled" : "other", R = E(O, R), g.push({
                style: R,
                ymd: r.y + "-" + jet.digit(r.m) + "-" + jet.digit(O),
                day: C(r.y, r.m, O) + Q
            });
            return g
        }, eachTime: function (a, b) {
            var c = this, d = c.$opts, e = d.range, f = d.multiPane, g = [], h = [], i = ["hh", "mm", "ss"], j = [],
                k = "", l = c.format, m = jet.trim(c.minDate).replace(/\s+/g, " "),
                n = jet.trim(c.maxDate).replace(/\s+/g, " "), o = m.split(" "), p = n.split(" ");
            return c.dlen > 3 && /\:/.test(o) && /\:/.test(p) && (g = jet.reMatch(/\s/.test(m) && c.dlen > 3 ? o[1] : m), h = jet.reMatch(/\s/.test(n) && c.dlen > 3 ? p[1] : n)), jet.each([24, 60, 60], function (d, m) {
                var n, p, q, r;
                for (j[d] = [], n = void 0 == g[d] || 0 == g[d] ? a[i[d]] : g[d], p = "" == c.getValue() ? n : a[i[d]], c.dlen > 3 && /\:/.test(o) && 1 == b && (c.selectTime[0][i[d]] = p), q = 0; m > q; q++) r = new RegExp(i[d], "g").test(l), k = q == p ? r ? "action" : "disabled" : !r || !e && f && (q < g[d] || q > h[d]) ? "disabled" : f ? "" : 1 == b && q < g[d] || 2 == b && q > h[d] ? "disabled" : "", j[d].push({
                    style: k,
                    hms: q
                })
            }), j
        }, closeDate: function () {
            var a = $Q(elx), b = $Q("#jedatetipscon");
            a && document.body.removeChild(a), b && document.body.removeChild(b), this.setDatas()
        }, parseValue: function (a, b) {
            var c = this, d = [], e = c.$opts, f = e.range;
            return jet.each(a, function (a, c) {
                d.push(jet.parse(c, b))
            }), 0 == f ? d[0] : d.join(f)
        }, dateRegExp: function (valArr) {
            var i, enval = valArr.split(",") || [], regs = "", doExp = function (val) {
                var arr, tmpEval, regs = /#?\{(.*?)\}/;
                for (val += ""; null != (arr = regs.exec(val));) arr.lastIndex = arr.index + arr[1].length + arr[0].length - arr[1].length - 1, tmpEval = parseInt(eval(arr[1])), 0 > tmpEval && (tmpEval = "9700" + -tmpEval), val = val.substring(0, arr.index) + tmpEval + val.substring(arr.lastIndex + 1);
                return val
            };
            if (enval && enval.length > 0) {
                for (i = 0; i < enval.length; i++) regs += doExp(enval[i]), i != enval.length - 1 && (regs += "|");
                regs = regs ? new RegExp("(?:" + regs + ")") : null
            } else regs = null;
            return regs
        }, showFestival: function () {
            var a = this, b = a.$opts;
            jet.each(a.dateCell.querySelectorAll(".daystable td"), function (c, d) {
                var e = jet.reMatch(jet.attr(d, "ymd")), f = document.createElement("div");
                d.removeAttribute("ymd"), jet.on(d, "mouseover", function () {
                    var d, g, h, i, j, c = new jeLunar(e[0], e[1] - 1, e[2]);
                    $Q("#jedatetipscon") || (f.id = f.className = "jedatetipscon", d = "<p>" + c.solarYear + "年" + c.solarMonth + "月" + c.solarDate + "日 " + c.inWeekDays + '</p><p class="red">农历：' + c.shengxiao + "年 " + c.lnongMonth + "月" + c.lnongDate + "</p><p>" + c.ganzhiYear + "年 " + c.ganzhiMonth + "月 " + c.ganzhiDate + "日</p>", g = "" != (c.solarFestival || c.lunarFestival) ? '<p class="red">' + ("节日：" + c.solarFestival + c.lunarFestival) + "</p>" : "", h = "" != c.jieqi ? '<p class="red">' + ("" != c.jieqi ? "节气：" + c.jieqi : "") + "</p>" : "", i = "" != (c.solarFestival || c.lunarFestival || c.jieqi) ? g + h : "", jet.html(f, d + i), document.body.appendChild(f), j = a.lunarOrien(f, this), jet.setCss(f, {
                        zIndex: void 0 == b.zIndex ? 10005 : b.zIndex + 5,
                        top: j.top,
                        left: j.left,
                        position: "absolute",
                        display: "block"
                    }))
                }), jet.on(d, "mouseout", function () {
                    document.body.removeChild($Q("#jedatetipscon"))
                })
            }), 1 !== a.dateCell.nodeType || jet.hasClass(a.dateCell, "grid") || (a.dateCell.className = a.dateCell.className + " grid")
        }, lunarOrien: function (a, b, c) {
            var d, e, f, g, h = b.getBoundingClientRect(), i = a.offsetWidth, j = a.offsetHeight;
            return e = h.right + i / 1.5 >= jet.docArea(!0) ? h.right - i : h.left + (c ? 0 : jet.docScroll(!0)), d = h.bottom + j / 1 <= jet.docArea() ? h.bottom - 1 : h.top > j / 1.5 ? h.top - j - 1 : jet.docArea() - j, e + i > jet.docArea(!0) && (e = h.left - (i - h.width)), f = Math.max(d + (c ? 0 : jet.docScroll()) + 1, 1) + "px", g = e + "px", {
                top: f,
                left: g
            }
        }, dateOrien: function (a, b, c) {
            var g, h, j, k, d = this, i = d.$opts.fixed ? b.getBoundingClientRect() : a.getBoundingClientRect(),
                f = i.left, e = i.bottom;
            d.$opts.fixed ? (j = a.offsetWidth, k = a.offsetHeight, f + j > jet.docArea(!0) && (f -= j - i.width), e + k > jet.docArea() && (e = i.top > k ? i.top - k - 2 : jet.docArea() - k - 1), g = Math.max(e + (c ? 0 : jet.docScroll()) + 1, 1) + "px", h = f + "px") : (g = "50%", h = "50%", a.style.cssText = "marginTop:" + -(i.height / 2) + ";marginLeft:" + -(i.width / 2)), jet.setCss(a, {
                top: g,
                left: h
            })
        }, tips: function (a, b) {
            var e, c = this, d = $Q(".jedate-tips", c.dateCell);
            jet.html(d, a || ""), jet.setCss(d, {display: "block"}), clearTimeout(e), e = setTimeout(function () {
                jet.html(d, ""), jet.setCss(d, {display: "none"})
            }, 1e3 * (b || 2.5))
        }, locateScroll: function () {
            var a = this, b = $Q(".jedate-time", a.dateCell).querySelectorAll("ul");
            jet.each(b, function (a, b) {
                var c = b.querySelector(".action");
                b.scrollTop = c ? c.offsetTop - 145 : 0
            }), 7 != a.dlen && jet.setCss($Q(".jedate-time", a.dateCell), {display: "none"})
        }
    }), jeDate
});
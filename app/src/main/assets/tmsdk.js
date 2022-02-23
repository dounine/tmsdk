function TMSDK() {
    this.init = false;
    this.appid = "";
    this.programId = "";
    this.channel = "";
    this.weixinLoginCallbackName = "weixinLoginCallback";
    this.weixinPayCallbackName = "weixinPayCallback";
    this.expireTime = 0;
}

TMSDK.prototype = {
    constructor: TMSDK,
    config: function ({
                          appid,
                          programId,
                          channel = "",
                          expireTime = 30 * 24 * 60 * 60 * 1000
                      }) {
        this.init = true;
        this.appid = appid;
        this.programId = programId;
        this.channel = channel;
        this.expireTime = expireTime;
        if(window.android.info && typeof(window.android.info)=="function"){
            this.info = JSON.parse(window.android.info());
        }else{
            this.info = {};
        }
        window.android.init(
            this.appid,
            this.programId,
            this.channel,
            this.weixinLoginCallbackName,
            this.weixinPayCallbackName
        );
    },
    /**
     * 拉起微信登录
     */
    weixinLogin: function () {
        let name = this.weixinLoginCallbackName;
        let init = this.init;
        let expireTime = this.expireTime;
        return new Promise(function (resolve, reject) {
            let loginInfo = localStorage.getItem('loginInfo');
            //30天缓存用户登录数据
            if (loginInfo && (loginInfo.cacheTime + expireTime) < new Date().getTime()) {
                loginInfo = null;
            }
            window[name] = function (data) {
                let copyData = {
                    ...data,
                    cacheTime: new Date().getTime()
                };
                resolve(copyData);
                localStorage.setItem('loginInfo', JSON.stringify(copyData));
            };
            if (init) {
                if (loginInfo) {
                    console.info("使用缓存:" + loginInfo);
                    resolve(JSON.parse(loginInfo));
                } else {
                    console.info("无缓存数据");
                    window.android.weixinLogin();
                }
            } else {
                let errorMsg = "请先调用config方法进行初始化";
                reject(errorMsg);
                console.error(errorMsg);
            }
        });
    },
    /**
     *  登录上报
     * @param userId 使用open_id或者union_id
     */
    loginReport: function ({userId}) {
        if (this.init) {
            window.android.loginReport(userId);
        } else {
            let errorMsg = "请先调用config方法进行初始化";
            reject(errorMsg);
            console.error(errorMsg);
        }
    },
    /**
     * 微信支付
     * coin 支付金额(单位角)
     * userId 支付用户id
     * programParam 支付回调参数(用于支付成功给服务回调使用)
     */
    weixinPay: function ({coin, userId, programParam, goodsName = "", zone = "", gameUid = "", gameNickname = ""}) {
        let name = this.weixinPayCallbackName;
        let init = this.init;
        let info = this.info;
        return new Promise(function (resolve, reject) {
            window[name] = function (data) {
                resolve(data);
            };
            if (init) {
                if(info.version){
                    window.android.weixinPay(coin, userId, programParam, goodsName, zone, gameUid, gameNickname)
                }else{
                    window.android.weixinPay(coin, userId, programParam, goodsName)
                }
            } else {
                let msg = "请先调用config方法进行初始化";
                reject(msg);
                console.error(msg);
            }
        });
    },
    /**
     * 微信定单查询
     * @param orderId 定单号
     */
    weixinOrderQuery: function ({orderId}) {
        return new Promise(function (resolve, reject) {
            resolve(window.android.queryOrder(payOrder.orderId));
        });
    },
    /**
     * 实名认证查询
     * @param userId 用户id
     * @returns {Promise<unknown>}
     */
    identifyQuery: function ({userId}) {
        return new Promise(function (resolve, reject) {
            resolve(window.android.identifyQuery(userId));
        })
    },
    /**
     * 实名认证
     * @param userId 用户id
     * @param name 姓名
     * @param id 身份证号
     * @returns {Promise<unknown>}
     */
    identify: function ({userId, name, id}) {
        return new Promise(function (resolve, reject) {
            resolve(window.android.identify(userId, name, id));
        })
    }
}
window.TMSdk = new TMSDK()
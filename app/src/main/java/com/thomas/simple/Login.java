package com.thomas.simple;

import com.thomas.rxpersistence.SPEntity;
import com.thomas.rxpersistence.SPField;

import java.util.List;

/**
 * Author: Thomas.<br/>
 * Date: 2020/1/15 14:11<br/>
 * GitHub: https://github.com/TanZhiL<br/>
 * CSDN: https://blog.csdn.net/weixin_42703445<br/>
 * Email: 1071931588@qq.com<br/>
 * Description:
 */
@SPEntity
public class Login {
    /**
     * channels : [{"abbrev":"string","address":"string","bindingStatus":0,"brandShopDiscountList":[{"brand":"string","category":"string","discount":0}],"channelId":"string","city":"string","code":"string","companyId":"string","fictitiousPerson":"string","goodsDiscounts":[{"goodsNo":"string","marketPlaceDiscount":0}],"group":"string","marketPlaceDiscount":0,"mathod":"string","mobilePhone":"string","name":"string","normalDiscount":0,"payCodeList":[{"notIncome":false,"notIntegral":false,"payAlias":"string","payCode":"string","payGuid":"string","payName":"string","payPlatformId":0,"relatePayGuid":"string"}],"phone":"string","promoBeforeVip":0,"region":"string","repOfficeId":"string","sort":"string","typeName":"string","uuid":"string","vipUserGroup":"string"}]
     * coordPlatformResps : [{"id":0,"platformName":"string","platformStatus":0,"platformWebsite":"string"}]
     * logisticsState : 0
     * logisticsUrl : string
     * minRebate : 0
     * password : string
     * saleCategoryMinRebates : [{"minRebateSaleCategoryId":"string","saleCategoryMinRebate":0}]
     * shutout : 0
     * token : string
     * userGuid : string
     * userId : string
     * userName : string
     */

    private int logisticsState;
    private String logisticsUrl;
    private int minRebate;
    private String password;
    private int shutout;
    private String token;
    private String userGuid;
    private String userId;
    private String userName;
    private ChannelsBean channelsBean;
    private List<ChannelsBean> channels;
    private List<CoordPlatformRespsBean> coordPlatformResps;
    private List<SaleCategoryMinRebatesBean> saleCategoryMinRebates;

    public int getLogisticsState() {
        return logisticsState;
    }

    public void setLogisticsState(int logisticsState) {
        this.logisticsState = logisticsState;
    }

    public String getLogisticsUrl() {
        return logisticsUrl;
    }

    public void setLogisticsUrl(String logisticsUrl) {
        this.logisticsUrl = logisticsUrl;
    }

    public int getMinRebate() {
        return minRebate;
    }

    public void setMinRebate(int minRebate) {
        this.minRebate = minRebate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getShutout() {
        return shutout;
    }

    public void setShutout(int shutout) {
        this.shutout = shutout;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserGuid() {
        return userGuid;
    }

    public void setUserGuid(String userGuid) {
        this.userGuid = userGuid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<ChannelsBean> getChannels() {
        return channels;
    }

    public void setChannels(List<ChannelsBean> channels) {
        this.channels = channels;
    }

    public List<CoordPlatformRespsBean> getCoordPlatformResps() {
        return coordPlatformResps;
    }

    public void setCoordPlatformResps(List<CoordPlatformRespsBean> coordPlatformResps) {
        this.coordPlatformResps = coordPlatformResps;
    }

    public List<SaleCategoryMinRebatesBean> getSaleCategoryMinRebates() {
        return saleCategoryMinRebates;
    }

    public void setSaleCategoryMinRebates(List<SaleCategoryMinRebatesBean> saleCategoryMinRebates) {
        this.saleCategoryMinRebates = saleCategoryMinRebates;
    }

    public static class ChannelsBean {
        /**
         * abbrev : string
         * address : string
         * bindingStatus : 0
         * brandShopDiscountList : [{"brand":"string","category":"string","discount":0}]
         * channelId : string
         * city : string
         * code : string
         * companyId : string
         * fictitiousPerson : string
         * goodsDiscounts : [{"goodsNo":"string","marketPlaceDiscount":0}]
         * group : string
         * marketPlaceDiscount : 0
         * mathod : string
         * mobilePhone : string
         * name : string
         * normalDiscount : 0
         * payCodeList : [{"notIncome":false,"notIntegral":false,"payAlias":"string","payCode":"string","payGuid":"string","payName":"string","payPlatformId":0,"relatePayGuid":"string"}]
         * phone : string
         * promoBeforeVip : 0
         * region : string
         * repOfficeId : string
         * sort : string
         * typeName : string
         * uuid : string
         * vipUserGroup : string
         */

        private String abbrev;
        private String address;
        private int bindingStatus;
        private String channelId;
        private String city;
        private String code;
        private String companyId;
        private String fictitiousPerson;
        private String group;
        private int marketPlaceDiscount;
        private String mathod;
        private String mobilePhone;
        private String name;
        private int normalDiscount;
        private String phone;
        private int promoBeforeVip;
        private String region;
        private String repOfficeId;
        private String sort;
        private String typeName;
        private String uuid;
        private String vipUserGroup;
        private List<BrandShopDiscountListBean> brandShopDiscountList;
        private List<GoodsDiscountsBean> goodsDiscounts;
        private List<PayCodeListBean> payCodeList;

        public String getAbbrev() {
            return abbrev;
        }

        public void setAbbrev(String abbrev) {
            this.abbrev = abbrev;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getBindingStatus() {
            return bindingStatus;
        }

        public void setBindingStatus(int bindingStatus) {
            this.bindingStatus = bindingStatus;
        }

        public String getChannelId() {
            return channelId;
        }

        public void setChannelId(String channelId) {
            this.channelId = channelId;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getCompanyId() {
            return companyId;
        }

        public void setCompanyId(String companyId) {
            this.companyId = companyId;
        }

        public String getFictitiousPerson() {
            return fictitiousPerson;
        }

        public void setFictitiousPerson(String fictitiousPerson) {
            this.fictitiousPerson = fictitiousPerson;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public int getMarketPlaceDiscount() {
            return marketPlaceDiscount;
        }

        public void setMarketPlaceDiscount(int marketPlaceDiscount) {
            this.marketPlaceDiscount = marketPlaceDiscount;
        }

        public String getMathod() {
            return mathod;
        }

        public void setMathod(String mathod) {
            this.mathod = mathod;
        }

        public String getMobilePhone() {
            return mobilePhone;
        }

        public void setMobilePhone(String mobilePhone) {
            this.mobilePhone = mobilePhone;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getNormalDiscount() {
            return normalDiscount;
        }

        public void setNormalDiscount(int normalDiscount) {
            this.normalDiscount = normalDiscount;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public int getPromoBeforeVip() {
            return promoBeforeVip;
        }

        public void setPromoBeforeVip(int promoBeforeVip) {
            this.promoBeforeVip = promoBeforeVip;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getRepOfficeId() {
            return repOfficeId;
        }

        public void setRepOfficeId(String repOfficeId) {
            this.repOfficeId = repOfficeId;
        }

        public String getSort() {
            return sort;
        }

        public void setSort(String sort) {
            this.sort = sort;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getVipUserGroup() {
            return vipUserGroup;
        }

        public void setVipUserGroup(String vipUserGroup) {
            this.vipUserGroup = vipUserGroup;
        }

        public List<BrandShopDiscountListBean> getBrandShopDiscountList() {
            return brandShopDiscountList;
        }

        public void setBrandShopDiscountList(List<BrandShopDiscountListBean> brandShopDiscountList) {
            this.brandShopDiscountList = brandShopDiscountList;
        }

        public List<GoodsDiscountsBean> getGoodsDiscounts() {
            return goodsDiscounts;
        }

        public void setGoodsDiscounts(List<GoodsDiscountsBean> goodsDiscounts) {
            this.goodsDiscounts = goodsDiscounts;
        }

        public List<PayCodeListBean> getPayCodeList() {
            return payCodeList;
        }

        public void setPayCodeList(List<PayCodeListBean> payCodeList) {
            this.payCodeList = payCodeList;
        }

        public static class BrandShopDiscountListBean {
            /**
             * brand : string
             * category : string
             * discount : 0
             */

            private String brand;
            private String category;
            private int discount;

            public String getBrand() {
                return brand;
            }

            public void setBrand(String brand) {
                this.brand = brand;
            }

            public String getCategory() {
                return category;
            }

            public void setCategory(String category) {
                this.category = category;
            }

            public int getDiscount() {
                return discount;
            }

            public void setDiscount(int discount) {
                this.discount = discount;
            }
        }

        public static class GoodsDiscountsBean {
            /**
             * goodsNo : string
             * marketPlaceDiscount : 0
             */

            private String goodsNo;
            private int marketPlaceDiscount;

            public String getGoodsNo() {
                return goodsNo;
            }

            public void setGoodsNo(String goodsNo) {
                this.goodsNo = goodsNo;
            }

            public int getMarketPlaceDiscount() {
                return marketPlaceDiscount;
            }

            public void setMarketPlaceDiscount(int marketPlaceDiscount) {
                this.marketPlaceDiscount = marketPlaceDiscount;
            }
        }

        public static class PayCodeListBean {
            /**
             * notIncome : false
             * notIntegral : false
             * payAlias : string
             * payCode : string
             * payGuid : string
             * payName : string
             * payPlatformId : 0
             * relatePayGuid : string
             */

            private boolean notIncome;
            private boolean notIntegral;
            private String payAlias;
            private String payCode;
            private String payGuid;
            private String payName;
            private int payPlatformId;
            private String relatePayGuid;

            public boolean isNotIncome() {
                return notIncome;
            }

            public void setNotIncome(boolean notIncome) {
                this.notIncome = notIncome;
            }

            public boolean isNotIntegral() {
                return notIntegral;
            }

            public void setNotIntegral(boolean notIntegral) {
                this.notIntegral = notIntegral;
            }

            public String getPayAlias() {
                return payAlias;
            }

            public void setPayAlias(String payAlias) {
                this.payAlias = payAlias;
            }

            public String getPayCode() {
                return payCode;
            }

            public void setPayCode(String payCode) {
                this.payCode = payCode;
            }

            public String getPayGuid() {
                return payGuid;
            }

            public void setPayGuid(String payGuid) {
                this.payGuid = payGuid;
            }

            public String getPayName() {
                return payName;
            }

            public void setPayName(String payName) {
                this.payName = payName;
            }

            public int getPayPlatformId() {
                return payPlatformId;
            }

            public void setPayPlatformId(int payPlatformId) {
                this.payPlatformId = payPlatformId;
            }

            public String getRelatePayGuid() {
                return relatePayGuid;
            }

            public void setRelatePayGuid(String relatePayGuid) {
                this.relatePayGuid = relatePayGuid;
            }
        }
    }

    public static class CoordPlatformRespsBean {
        /**
         * id : 0
         * platformName : string
         * platformStatus : 0
         * platformWebsite : string
         */

        private int id;
        private String platformName;
        private int platformStatus;
        private String platformWebsite;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getPlatformName() {
            return platformName;
        }

        public void setPlatformName(String platformName) {
            this.platformName = platformName;
        }

        public int getPlatformStatus() {
            return platformStatus;
        }

        public void setPlatformStatus(int platformStatus) {
            this.platformStatus = platformStatus;
        }

        public String getPlatformWebsite() {
            return platformWebsite;
        }

        public void setPlatformWebsite(String platformWebsite) {
            this.platformWebsite = platformWebsite;
        }
    }

    public static class SaleCategoryMinRebatesBean {
        /**
         * minRebateSaleCategoryId : string
         * saleCategoryMinRebate : 0
         */

        private String minRebateSaleCategoryId;
        private int saleCategoryMinRebate;

        public String getMinRebateSaleCategoryId() {
            return minRebateSaleCategoryId;
        }

        public void setMinRebateSaleCategoryId(String minRebateSaleCategoryId) {
            this.minRebateSaleCategoryId = minRebateSaleCategoryId;
        }

        public int getSaleCategoryMinRebate() {
            return saleCategoryMinRebate;
        }

        public void setSaleCategoryMinRebate(int saleCategoryMinRebate) {
            this.saleCategoryMinRebate = saleCategoryMinRebate;
        }
    }

    public ChannelsBean getChannelsBean() {
        return channelsBean;
    }

    public void setChannelsBean(ChannelsBean channelsBean) {
        this.channelsBean = channelsBean;
    }
}

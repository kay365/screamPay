var myApp = angular.module("myApp", [])
.controller("myController",function($scope,$http,$interval,$timeout){
	//常用变量信息
	$scope.hideSureInfoBtn = true;//确认信息延迟
	$scope.bank_savings = bank_savings;//储蓄卡列表
	$scope.bindCardSMS = bindCardSMS;//储蓄卡列表
	$scope.bank_saving = [];
	$scope.surePayBtn = true;//确认支付延迟
	$scope.userSigns = userSigns;//签约银行卡列表
	$scope.bankCodeDesc = bankCodeDesc;//银行卡关系描述对象
	$scope.cardListShow = true;//签约卡列表隐藏
	$scope.showTheNewCard = false;//绑定卡的隐藏
	$scope.bindDaoJiShi = true;//绑卡倒计时隐藏
	$scope.bindCardSMS = bindCardSMS;//判断时候要显示发送验证码
	$scope.resendSMS = resendSMS;
	$scope.sureShowYzm = false;//隐藏发送验证码显示框
	$scope.payDaoJiShi = true;//确定支付的重新发送验证码按钮
	$scope.resendSMSShow = true;
	$scope.tipsShow = false;//消息提示框隐藏
	$scope.successfulPayDiv = false;//成功支付显示东西
	
	for(var i in $scope.bank_savings){
		$scope.bank_saving.push({"name": i , "bankName":$scope.bank_savings[i]});
	}
	/**
	 * 
	 *添加新卡
	 * 
	 */
	$scope.addNewCard = function(){
		if($scope.bindCardSMS == 0){
			$scope.sureShowYzm = false;
		}else{
			$scope.sureShowYzm = true;
		}
		$scope.showTheNewCard = true;
		$scope.cardListShow = false;
	}
	
	/**
	 * 
	 *绑定银行卡获取验证码
	 * 
	 */
	$scope.getConfirmYzm = function(){
		if($scope.userName!=undefined&&$scope.cardPhone!=undefined&&$scope.cardBank!=undefined&&$scope.IDCard!=undefined&&$scope.cardNumber!=undefined){
			$http({
	            method: 'POST',
	            url: "/pay/card/bind?time="+(new Date()).getTime(),
	            headers: { "platform" : 1 , "vs" : "", "sno": "", "ts" : "" , "sign" : ""},
	            params:{
	            	"merchNo": merchNo,               //商户号
	            	"orderNo": orderNo,               //订单号
	            	"acctName": $scope.userName,      //姓名
	            	"phone": $scope.cardPhone,        //电话
	            	"bankCode": $scope.cardBank,       //所属银行
	            	"certType": 1,                    //1为身份证号
	            	"certNo":  $scope.IDCard,         //身份证号
	            	"cardType": 0,                    //银行卡类型 0储蓄卡
	            	"bankNo": $scope.cardNumber.replace(/\s+/g,""),      //银行卡号
	            	"validDate": "",                    //信用卡日期
	            	"cvv2": ""                         //信用卡背面信息
	            }
	        }).then(function successCallback(response){
	            if(response.data.code == 0){
	            	$scope.sign = response.data.sign;//绑卡成功返回签约信息
	            	$scope.yzmNumber = 60;//获取验证码时间为倒计时60秒
	            	$scope.bindDaoJiShi = false;
	            	 var timer = $interval(function(){
                         $scope.yzmNumber -= 1;
                         $scope.yzmNumberString = $scope.yzmNumber+"s";
                         if($scope.yzmNumber <= 0){
                             $interval.cancel(timer);
                             $scope.bindDaoJiShi = true;
                         }
                     },1000);
	            }else if(response.data.code == 1){
	            	alert(response.data.msg)
	            }
	        },function error(){
	            // 请求失败执行代码
	        });
		}else{
			alert("用户信息不能为空！")
		}
	}
	
	/**
	 * 
	 *确认信息  
	 * 
	 */
	$scope.sureInfoMationFun = function(){
		
		if($scope.userName!=undefined&&$scope.cardPhone!=undefined&&$scope.cardBank!=undefined&&$scope.IDCard!=undefined&&$scope.cardNumber!=undefined&& ($scope.cardBindYzm!=undefined || $scope.bindCardSMS == 0)){
			$scope.hideSureInfoBtn = false;
			if($scope.bindCardSMS == 0){
				$scope.sign = "11";
				$scope.cardBindYzm = "9567";
			}
			$http({
	            method: 'POST',
	            url: "/pay/card/bind/confirm?time="+(new Date()).getTime(),
	            headers: { "platform" : 1 , "vs" : "", "sno": "", "ts" : "" , "sign" : ""},
	            params:{
	            	"merchNo": merchNo,               //商户号
	            	"orderNo": orderNo,               //订单号
	            	"sign": $scope.sign,              //签约号
	            	"checkCode": $scope.cardBindYzm,   //验证码
	            	"acctName": $scope.userName,      //姓名
	            	"phone": $scope.cardPhone,        //电话
	            	"bankCode": $scope.cardBank,       //所属银行
	            	"certType": 1,                    //1为身份证号
	            	"certNo":  $scope.IDCard,         //身份证号
	            	"cardType": 0,                    //银行卡类型 0储蓄卡
	            	"bankNo": $scope.cardNumber.replace(/\s+/g,""),      //银行卡号
	            	"validDate": "",                    //信用卡日期
	            	"cvv2": ""                         //信用卡背面信息
	            }
	        }).then(function successCallback(response){
	            if(response.data.code == 0){
	            	$scope.showgetYzmDiv = true;
	            	$scope.showTheNewCard = false;
	            }else{
	            	$scope.hideSureInfoBtn = true;
	            	alert(response.data.msg);
	            }
	        },function error(){
	            // 请求失败执行代码
	        });
		}
	};
	
	/**
	 * 
	 *确认支付  
	 * 
	 */
	$scope.surePayFun = function(){
		$scope.surePayBtn = false;
		if($scope.surePayYzm!=undefined){
			$http({
	            method: 'POST',
	            url: "/pay/card/pay/confirm?time="+(new Date()).getTime(),
	            headers: { "platform" : 1 , "vs" : "", "sno": "", "ts" : "" , "sign" : ""},
	            params:{
	            	"merchNo": merchNo,               //商户号
	            	"orderNo": orderNo,               //订单号
	            	"checkCode": $scope.surePayYzm    //验证码
	            }
	        }).then(function successCallback(response){
	            if(response.data.code == 0){
	            	//成功支付以后跳转页面
	            	$scope.tipsShow = true;
	            	$scope.ajaxMsg = "支付成功！";
	            	$timeout(function(){
	            		$scope.tipsShow = false;
	            		$scope.showgetYzmDiv = false;
                        $scope.successfulPayDiv = true;
					},1000);
	            }else{
	            	$scope.surePayBtn = true;
	            	$scope.tipsShow = true;
	            	$scope.ajaxMsg = "您本次支付失败，请重新支付!";
	            	$timeout(function(){
	            		$scope.tipsShow = false;
					},1000);
	            }
	        },function error(){
	            // 请求失败执行代码
	        });
		}
	}
	
	/**
	 * 
	 * 点击银行卡 快捷支付
	 * 
	 * **/
	
	$scope.sureQuickPay = function(index){
		$scope.clickSign = $scope.userSigns[index].sign;
		$http({
            method: 'POST',
            url: "/pay/card/pay?time="+(new Date()).getTime(),
            headers: { "platform" : 1 , "vs" : "", "sno": "", "ts" : "" , "sign" : ""},
            params:{
            	"merchNo": merchNo,               //商户号
            	"orderNo": orderNo,               //订单号
            	"sign": $scope.userSigns[index].sign,//签约信息
            	"bankNo" : $scope.userSigns[index].bankNo,
            	"bankCode" : $scope.userSigns[index].bankCode,
            	"cardType" : $scope.userSigns[index].cardType
            }
        }).then(function successCallback(response){
            if(response.data.code == 0){
            	$scope.showgetYzmDiv = true;
        		$scope.cardListShow = false;
	    		if($scope.resendSMS == 0){
	    			$scope.resendSMSShow = false;
	    		}else{
	    			$scope.resendSMSShow = true;
	    		}
            	$scope.yzmPayNumber = 60;//获取验证码时间为倒计时60秒
            	$scope.payDaoJiShi = false;
            	 var timer2 = $interval(function(){
                     $scope.yzmPayNumber -= 1;
                     $scope.yzmPayNumberString = $scope.yzmPayNumber+"秒";
                     if($scope.yzmPayNumber <= 0){
                         $interval.cancel(timer2);
                         $scope.payDaoJiShi = true;
                     }
                 },1000);
            	
            }else{
            	alert(response.data.msg);
            }
        },function error(){
            // 请求失败执行代码
        });
		
	}
	
	/**
	 * 
	 *重新获取验证码按钮
	 * 
	 */
	$scope.secondYzmGet = function(index){
		$http({
	            method: 'POST',
	            url: "/pay/card/msgResend?time="+(new Date()).getTime(),
	            headers: { "platform" : 1 , "vs" : "", "sno": "", "ts" : "" , "sign" : ""},
	            params:{
	            	"merchNo": merchNo,               //商户号
	            	"orderNo": orderNo,               //订单号
	            	"sign": $scope.clickSign,//签约信息
	            	"sendType": 2                     //2为确认支付重发验证码
	            }
	        }).then(function successCallback(response){
	            if(response.data.code == 0){
	            	$scope.yzmPayNumber = 60;//获取验证码时间为倒计时60秒
	            	$scope.payDaoJiShi = false;
	            	 var timer3 = $interval(function(){
	                     $scope.yzmPayNumber -= 1;
	                     $scope.yzmPayNumberString = $scope.yzmPayNumber+"秒";
	                     if($scope.yzmPayNumber <= 0){
	                         $interval.cancel(timer3);
	                         $scope.payDaoJiShi = true;
	                     }
	                 },1000);
	            	
	            }else{
	            	alert(response.data.msg);
	            }
	        },function error(){
	            // 请求失败执行代码
	        });
	}
});
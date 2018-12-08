
package com.qh.paythird.hx.wsclient;

/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

import java.net.URL;
import javax.xml.namespace.QName;


/**
 * 扫码
 * 
 */
public final class ScanService_WSScanSoap_Client {

    private static final QName SERVICE_NAME = new QName("http://payat.ips.com.cn/WebService/Scan", "WSScan");

    /**
     * 
     * @Description 扫码
     * @param xml
     * @return
     */
    public static String scanPay(String xml){
        URL wsdlURL = WSScan.WSDL_LOCATION;
        WSScan ss = new WSScan(wsdlURL, SERVICE_NAME);
        ScanService port = ss.getWSScanSoap();
        return port.scanPay(xml);
    }
    
    public static void main(String[] args) {
    	String xml = "<Ips><GateWayReq><head><Version>v1.0.0</Version><MerCode>184045</MerCode><MerName>idcbuy</MerName><Account>1840450017</Account><MsgId>20170105191551364552</MsgId><ReqDate>20170105191552</ReqDate><Signature>8a702f419044a0952fd9efd893b82479</Signature></head><body><MerBillNo>20170105191551364552</MerBillNo><GatewayType>10</GatewayType><Date>20170105</Date><CurrencyType>156</CurrencyType><Amount>1.00</Amount><Lang>GB</Lang><Attach>20170105191551364552</Attach><RetEncodeType>17</RetEncodeType><ServerUrl>http://www.312157.top/do/newipn2016wei/fmnos.php</ServerUrl><BillEXP>2</BillEXP><GoodsName>20170105191551364552</GoodsName></body></GateWayReq></Ips>";

		System.out.println(scanPay(xml));
	}

}

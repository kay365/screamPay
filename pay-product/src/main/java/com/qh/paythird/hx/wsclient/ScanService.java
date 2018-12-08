package com.qh.paythird.hx.wsclient;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * 扫码
 * @author Administrator
 *
 */
@WebService(name = "ScanService", targetNamespace = "http://payat.ips.com.cn/WebService/Scan")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface ScanService {

	/**
     * 
     * @param scanPayReq
     * @return
     *     returns java.lang.String
     */
    @WebMethod(action = "http://payat.ips.com.cn/WebService/Scan/scanPay")
    @WebResult(name = "scanPayRsp", partName = "scanPayRsp",targetNamespace = "http://payat.ips.com.cn/WebService/Scan")
    public String scanPay(
        @WebParam(name = "scanPayReq", partName = "scanPayReq")
        String scanPayReq);

    /**
     * 
     * @param barCodeScanPay
     * @return
     *     returns java.lang.String
     */
    @WebMethod(action = "http://payat.ips.com.cn/WebService/Scan/barCodeScanPay")
    @WebResult(name = "barCodeScanPayResult", partName = "barCodeScanPayResult" ,targetNamespace = "http://payat.ips.com.cn/WebService/Scan")
    public String barCodeScanPay(
        @WebParam(name = "barCodeScanPay", partName = "barCodeScanPay")
        String barCodeScanPay);
}

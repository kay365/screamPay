package com.qh.paythird.hx.wsclient;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * 扫码
 * 
 */
    @WebServiceClient(name = "WSScan",
                      wsdlLocation = "https://thumbpay.e-years.com/psfp-webscan/services/scan?wsdl",
                      targetNamespace = "http://payat.ips.com.cn/WebService/Scan")
public class WSScan extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://payat.ips.com.cn/WebService/Scan", "WSScan");
    public final static QName WSScanSoap = new QName("http://payat.ips.com.cn/WebService/Scan", "WSScanSoap");
    static {
        URL url = null;
        try {
            url = new URL("https://thumbpay.e-years.com/psfp-webscan/services/scan?wsdl");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(WSScan.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "https://thumbpay.e-years.com/psfp-webscan/services/scan?wsdl");
        }
        WSDL_LOCATION = url;
    }

    public WSScan(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public WSScan(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public WSScan() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    public WSScan(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    public WSScan(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    public WSScan(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }    




    /**
     *
     * @return
     *     returns ScanService
     */
    @WebEndpoint(name = "WSScanSoap")
    public ScanService getWSScanSoap() {
        return super.getPort(WSScanSoap, ScanService.class);
    }

    /**
     * 
     * @return
     *     returns ScanService
     */
    @WebEndpoint(name = "WSScanSoap")
    public ScanService getWSScanSoap(WebServiceFeature... features) {
        return super.getPort(WSScanSoap, ScanService.class, features);
    }

}

beans{
	xmlns cxf: "http://camel.apache.org/schema/cxf"
	xmlns jaxrs: "http://cxf.apache.org/jaxrs"
	xmlns util: "http://www.springframework.org/schema/util"
	
	echoService(org.onap.aai.datarouter.service.EchoService)

	
	util.list(id: 'echoServices') {	
		ref(bean:'echoService')
	}
}

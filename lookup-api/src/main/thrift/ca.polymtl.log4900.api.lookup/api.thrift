namespace scala ca.polymtl.log4900.api.lookup

struct ServiceInfo {
	1: string name,
	2: string host,
	3: i32 port
}

service Lookup {
	void register(1: ServiceInfo info)
}
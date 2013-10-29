namespace scala ca.polymtl.log4900.api.eval

service Insight {
	string eval(1: string code)
	list<string> codeComplete(1: string code, 2: i32 pos)
}
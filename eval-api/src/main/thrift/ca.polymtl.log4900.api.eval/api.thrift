namespace scala ca.polymtl.log4900.api.eval

enum Severity { INFO, WARNING, ERROR }

struct CompilationInfo {
	string message,
	i32 pos,
	Severity severity
}

struct Result {
	string insight,
	string output,
	list<CompilationInfo> infos,
	list<string> completions
}

service Insight {
	Result eval(1: string code, 2: i32 pos)
}
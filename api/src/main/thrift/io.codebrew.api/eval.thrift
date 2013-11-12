namespace scala io.codebrew.api.eval

enum Severity { INFO, WARNING, ERROR }

struct CompilationInfo {
	1: string message,
	2: i32 pos,
	3: Severity severity
}

struct InsightResult {
	1: string insight,
	2: string output
}

struct Result {
	1: optional InsightResult insight,
	2: list<CompilationInfo> infos
}

service Eval {
	Result insight(1: string code),
	list<string> autocomplete(1: string code, 2: i32 pos)
}
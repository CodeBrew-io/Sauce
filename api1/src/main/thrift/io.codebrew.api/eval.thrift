namespace java io.codebrew.api.eval

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
	2: list<CompilationInfo> infos,
	3: bool timeout
}

struct Completion {
    1: string name,
    2: string signature
}

service Eval {
	Result insight(1: string code),
	list<Completion> autocomplete(1: string code, 2: i32 pos)
}
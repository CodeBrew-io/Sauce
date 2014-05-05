namespace java io.codebrew.api.eval

enum Severity { INFO, WARNING, ERROR }

struct CompilationInfo {
	1: string message,
	2: i32 pos,
	3: Severity severity
}

enum InstrumentationType {CODE}

struct Instrumentation {
	1: i32 line,
	2: string result,
	3: InstrumentationType itype
}

struct Result {
	1: optional list<Instrumentation> insight,
	2: list<CompilationInfo> infos,
	3: bool timeout,
	4: optional string runtimeError
}

struct Completion {
    1: string name,
    2: string signature
}

service Eval {
	Result insight(1: string code),
	list<Completion> autocomplete(1: string code, 2: i32 pos)
}
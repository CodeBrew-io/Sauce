namespace scala ca.polymtl.log4900.api

struct User {
	1: string firstName,
	2: string lastName
}

service HelloUser {
	string hello(1: User user)
}
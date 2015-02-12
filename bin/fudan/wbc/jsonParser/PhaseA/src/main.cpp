#include "common.h"
#include "TermExtractor.h"

int main(int argc, char **argv)
{
	TermExtractor *pTermExtractor = new TermExtractor();
	pTermExtractor->parse(argv[1]);
	delete pTermExtractor;
	return 0;
}


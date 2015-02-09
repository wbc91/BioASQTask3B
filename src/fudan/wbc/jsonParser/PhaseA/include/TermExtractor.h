#include "common.h"

class TermExtractor{
public:
	TermExtractor();
	~TermExtractor(){};

	void parse(char *s);
protected:
	FILE *fp;
};
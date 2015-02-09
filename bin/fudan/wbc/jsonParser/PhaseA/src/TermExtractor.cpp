#include "../include/TermExtractor.h"
#include "stdio.h"


TermExtractor::TermExtractor():fp(nullptr)
{
}

void TermExtractor::parse(char *s)
{
	fp = fopen(s,"r");
	char c;
	std::string data = "";
	while(!feof(fp))
	{
		c = fgetc(fp);
		if(!feof(fp))
		{
			data+=c;
		}
	}
	fclose(fp);
	
	printf("%s\n", data.c_str());
	rapidjson::Document doc;
	doc.Parse<0>(data.c_str());
	auto& t = doc[1];
	
	for(int i = 0; i < t.Size(); ++i)
	{
		auto& ele = t[i];
		if(ele.IsInt()) printf("%d\n", ele.GetInt());
		else if(ele.IsString()) printf("%d\n",ele.GetString());
		else if(ele.IsBool()) 
			if(ele.GetBool())printf("true\n");
			else printf("false\n");
		else
			printf("flag error\n");
	}
}
function main() {
	logger.log("Executing script");
	var c = requestbody.content;
	var ret = eval("("+c+")");
	model.results = jsonUtils.toJSONString(ret);
	logger.log("Script executed");
}

//Start webscript
main();

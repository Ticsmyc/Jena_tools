package com.lc.playground;

import com.lc.rdf.impl;

public class Main {


    public static void main(String[] args) {
        if(args.length == 0 ){
            printUsage();
            return ;
        }
        switch(args[0]){
            case "-convertToNt":
                if (args.length != 3) {
                    System.out.println("usage: -convertToNt"+ " <inputDir> <outputFile> ");
                    break;
                }
                impl.Nt_conversion(args[1],args[2]);
                break;
            case "-convertToOther":
                if (args.length != 5) {
                    System.out.println("usage: -convertToOther"+ " <inputDir> <outputFile> <oldFormat> <newFormat> ");
                    break;
                }
                impl.format_conversion(args[1], args[2], args[3], args[4]);
                break;
            case "-count_triples":
                if (args.length != 2) {
                    System.out.println("usage: -count_triples"+ " <fileDir> ");
                    break;
                }
                impl.count_triples(args[1]);
                break;
            case "-iterator_triple":
                if (args.length != 3) {
                    System.out.println("usage: -iterator_triple"+ " <rdfFileDir> <csvFileDir>");
                    break;
                }
                impl.iterator_triple(args[1],args[2]);
                break;
            case "-csvToNt":
                if (args.length != 4) {
                    System.out.println("usage: -csvToRdf"+ " <csvFileDir> <rdfFileDir> <prefix>");
                    break;
                }
                impl.csvToNt(args[1],args[2],args[3]);
                break;
            case "-sparql_query" :
                if(args.length!=4){
                    System.out.println("usage: -sparql_query"+ " <rdfFileDir> <sparqlFileDir> <ResultPath>");
                    break;
                }
                impl.sparql_query(args[1],args[2],args[3]);
                break;
            default:
                System.out.println("参数错误~");
                printUsage();
        }
    }
    private static void printUsage(){
        System.out.println("用法: ");
        System.out.println("-------------------------格式转换-----------------------");
        System.out.println("将任意格式转为nt:   -convertToNt <inputDir> <outputFile>");
        System.out.println("两种任意格式转换:   -convertToOther <inputDir> <outputFile> <oldFormat> <newFormat>");
        System.out.println("(亲测可用格式：\"RDF/XML\",\"RDF/XML-ABBREV\",\"N-TRIPLE\",\"N3\",\"TTL\")");
        System.out.println("--------------------三元组统计、csv导入导出-----------------------");
        System.out.println("统计任意格式的三元组个数:   -count_triples <fileDir>");
        System.out.println("遍历任何格式,将三元组写入csv(自动去掉前后缀) :   -iterator_triple <rdfFileDir> <csvFileDir>");
        System.out.println("读取csv，转成nt格式 :   -csvToNt <csvFileDir> <rdfFileDir> <prefix>");
        System.out.println("--------------------------查询-----------------------");
        System.out.println("执行sparql查询 :   -sparql_query <rdfFileDir> <sparqlFileDir> <ResultPath>");
    }



}

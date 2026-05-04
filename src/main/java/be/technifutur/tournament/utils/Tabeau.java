package be.technifutur.tournament.utils;

import com.google.gson.internal.bind.util.ISO8601Utils;

import java.util.Arrays;

public class Tabeau {

    //sb est une string avec * comme separation de ligne
    //tabLen est la longeur du string le plus long pour qu on puisse adapter le tableau
    public static String displayInbox(String color,StringBuilder sb){
        String ret="";
        String theString = sb.toString();
        String[] parts = theString.split("\\*");

        //get la string la plus longue du tableau avec un stream
        int tabLen= Arrays.stream(parts)
                .mapToInt(String::length)
                .max()
                .orElse(0);

        String midLine="";
        for(int i=0;i<tabLen;i++){
            midLine+="─";
        }
        String startU="┌";
        String endU="┐\n";
        String startDw="└";
        String endD="┘\n";
        String resetColor= color.isBlank()? Dsg.r : color;

        ret+=color+startU+midLine+endU;
        for(String part : parts){
            int p= part.length();
            int insideLen = tabLen-p;
            int offset= (insideLen%2)==1?1:0;
            String redColor = part.contains("0. Quitter")? Dsg.re:"";
            ret+=String.format("│"+ redColor+" ".repeat(insideLen/2)+"%s"+ " ".repeat(insideLen/2+offset)+resetColor+"│\n",part);
        }
            ret+=startDw+midLine+endD+ Dsg.r;
        return ret;
    }

    public void test(){
        System.out.println("┌──────┬──────┐");
        System.out.println("│      │      │");
        System.out.println("├──────┼──────┤");
        System.out.println("│      │      │");
        System.out.println("│      │      │");
        System.out.println("└──────┴──────┘");
    }
}

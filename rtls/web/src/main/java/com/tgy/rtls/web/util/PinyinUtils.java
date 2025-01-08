package com.tgy.rtls.web.util;


import com.tgy.rtls.data.entity.user.Instance;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PinyinUtils {

	private static String toHanyuPinyin(String str) throws BadHanyuPinyinOutputFormatCombination {
		char[] charArray = str.toCharArray();
		StringBuilder pinyin = new StringBuilder();
		HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();
		outputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		for (int i = 0; i < charArray.length; i++) {
			//匹配中文,非中文转换会转换成null
            if (Character.toString(charArray[i]).matches("[\\u4E00-\\u9FA5]+")) {
            	String[] hanyuPinyinStringArray;
				try {
					hanyuPinyinStringArray = PinyinHelper.toHanyuPinyinStringArray(charArray[i], outputFormat);
					String string =hanyuPinyinStringArray[0];
					pinyin.append(string);
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					throw e;
				}
            }else{
                pinyin.append(charArray[i]);
            }
		}
		return pinyin.toString();
	}
	
	public static void main(String[] args) {
		String pinyin;
		try {
			pinyin = PinyinUtils.toHanyuPinyin("李四01");
			System.out.println(pinyin);
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		}
	}

	public static Map<String,List<Instance>> pinyinInstance(List<Instance> Instances) {
		Map<String, List<Instance>> mapInstance = new HashMap<>();
		try {
		List<Instance> InstanceA=new ArrayList<>();
		List<Instance> InstanceB=new ArrayList<>();
		List<Instance> InstanceC=new ArrayList<>();
		List<Instance> InstanceD=new ArrayList<>();
		List<Instance> InstanceE=new ArrayList<>();
		List<Instance> InstanceF=new ArrayList<>();
		List<Instance> InstanceG=new ArrayList<>();
		List<Instance> InstanceH=new ArrayList<>();
		List<Instance> InstanceI=new ArrayList<>();
		List<Instance> InstanceJ=new ArrayList<>();
		List<Instance> InstanceK=new ArrayList<>();
		List<Instance> InstanceL=new ArrayList<>();
		List<Instance> InstanceM=new ArrayList<>();
		List<Instance> InstanceN=new ArrayList<>();
		List<Instance> InstanceO=new ArrayList<>();
		List<Instance> InstanceP=new ArrayList<>();
		List<Instance> InstanceQ=new ArrayList<>();
		List<Instance> InstanceR=new ArrayList<>();
		List<Instance> InstanceS=new ArrayList<>();
		List<Instance> InstanceT=new ArrayList<>();
		List<Instance> InstanceU=new ArrayList<>();
		List<Instance> InstanceV=new ArrayList<>();
		List<Instance> InstanceW=new ArrayList<>();
		List<Instance> InstanceX=new ArrayList<>();
		List<Instance> InstanceY=new ArrayList<>();
		List<Instance> InstanceZ=new ArrayList<>();
		List<Instance> Instance1=new ArrayList<>();
		for (Instance Instance : Instances) {
			String pinyin = PinyinUtils.toHanyuPinyin(Instance.getName());
			String letter=pinyin.substring(0,1);
			switch (letter){
				case "a":
					InstanceA.add(Instance);
					break;
				case "b":
					InstanceB.add(Instance);
					break;
				case "c":
					InstanceC.add(Instance);
					break;
				case "d":
					InstanceD.add(Instance);
					break;
				case "e":
					InstanceE.add(Instance);
					break;
				case "f":
					InstanceF.add(Instance);
					break;
				case "g":
					InstanceG.add(Instance);
					break;
				case "h":
					InstanceH.add(Instance);
					break;
				case "i":
					InstanceI.add(Instance);
					break;
				case "j":
					InstanceJ.add(Instance);
					break;
				case "k":
					InstanceK.add(Instance);
					break;
				case "l":
					InstanceL.add(Instance);
					break;
				case "m":
					InstanceM.add(Instance);
					break;
				case "n":
					InstanceN.add(Instance);
					break;
				case "o":
					InstanceO.add(Instance);
					break;
				case "p":
					InstanceP.add(Instance);
					break;
				case "q":
					InstanceQ.add(Instance);
					break;
				case "r":
					InstanceR.add(Instance);
					break;
				case "s":
					InstanceS.add(Instance);
					break;
				case "t":
					InstanceT.add(Instance);
					break;
				case "u":
					InstanceU.add(Instance);
					break;
				case "v":
					InstanceV.add(Instance);
					break;
				case "w":
					InstanceW.add(Instance);
					break;
				case "x":
					InstanceX.add(Instance);
					break;
				case "y":
					InstanceY.add(Instance);
					break;
				case "z":
					InstanceZ.add(Instance);
					break;
				default:
					Instance1.add(Instance);
					break;
			}
		}
			mapInstance.put("A",InstanceA);
			mapInstance.put("B",InstanceB);
			mapInstance.put("C",InstanceC);
			mapInstance.put("D",InstanceD);
			mapInstance.put("E",InstanceE);
			mapInstance.put("F",InstanceF);
			mapInstance.put("G",InstanceG);
			mapInstance.put("H",InstanceH);
			mapInstance.put("I",InstanceI);
			mapInstance.put("J",InstanceJ);
			mapInstance.put("K",InstanceK);
			mapInstance.put("L",InstanceL);
			mapInstance.put("M",InstanceM);
			mapInstance.put("N",InstanceN);
			mapInstance.put("O",InstanceO);
			mapInstance.put("P",InstanceP);
			mapInstance.put("Q",InstanceQ);
			mapInstance.put("R",InstanceR);
			mapInstance.put("S",InstanceS);
			mapInstance.put("T",InstanceT);
			mapInstance.put("U",InstanceU);
			mapInstance.put("V",InstanceV);
			mapInstance.put("W",InstanceW);
			mapInstance.put("X",InstanceX);
			mapInstance.put("Y",InstanceY);
			mapInstance.put("Z",InstanceZ);
			mapInstance.put("其它",Instance1);
		}catch (BadHanyuPinyinOutputFormatCombination e) {
				e.printStackTrace();
		}
		return mapInstance;
	}
}

package inteldt.TodoML.classifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.JointClassification;
import com.aliasi.classify.JointClassifier;
import com.aliasi.lm.NGramBoundaryLM;
import com.aliasi.lm.NGramProcessLM;
import com.aliasi.lm.TokenizedLM;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;

/**
 * 文本分类的语言模型（N-gram）实现，可以分为基于字符的语言模型和基于词的语言模型
 * 
 * 调用Lingpipe的方法
 * 
 * @author shisi
 * @version 1.0   2015/12/11
 */
public class LMTextClassifier {
	private List<String> categorys; // 类别set
	private List<Classified<CharSequence>> classifiedlist;
	private DynamicLMClassifier<NGramBoundaryLM> boundaryLMClassifier; // boundary 语言模型（字符）
	private DynamicLMClassifier<NGramProcessLM> processLMClassifier; // process 语言模型 （字符）
	private DynamicLMClassifier<TokenizedLM> tokenizedLMClassifier; // 词 语言模型
	private JointClassifier<CharSequence> compiledClassifier;
	
	private int ngram = 6;
	
	public LMTextClassifier(){
		categorys = new ArrayList<String>();
		classifiedlist = new ArrayList<Classified<CharSequence>>();
	}
	
	/**
	 * 添加样本
	 */
	public void addSample(String text, String category){
		if(!categorys.contains(category)){
			categorys.add(category); // 如新加的样本，类别集中没有，则添加
		}
		
	   Classification classification = new Classification(category); 
       classifiedlist.add(new Classified<CharSequence>(text,classification));
	}
	
	public void setNGramSize(int ngram){
		this.ngram = ngram;
	}
	
	/**
	 * 通过样本训练模型,使用 process N-Gram模型，是一种字符Ngram模型
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	public void trainByProcessNGram() throws ClassNotFoundException, IOException{
		processLMClassifier = DynamicLMClassifier.createNGramProcess(categorys.toArray(new String[categorys.size()]), ngram); 
		for(Classified<CharSequence> classified :classifiedlist){
			processLMClassifier.handle(classified);
		}
	    
	   //compiling，提高分类效率
//       System.out.println("Compiling");
		 compiledClassifier = (JointClassifier<CharSequence>) AbstractExternalizable.compile(processLMClassifier);
		 
		 boundaryLMClassifier = null; // 如果用户使用process n-gram模型训练，boundaryLMClassifier失效，释放内存
	}
	
	
	/**
	 * 通过样本训练模型,使用 boundary N-Gram模型，是一种字符Ngram模型
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	public void trainByBoundaryNGram() throws ClassNotFoundException, IOException{
		boundaryLMClassifier = DynamicLMClassifier.createNGramBoundary(categorys.toArray(new String[categorys.size()]), ngram); 
		for(Classified<CharSequence> classified :classifiedlist){
			boundaryLMClassifier.handle(classified);
		}
	   
	   //compiling，提高分类效率
//       System.out.println("Compiling");
		 compiledClassifier = (JointClassifier<CharSequence>) AbstractExternalizable.compile(boundaryLMClassifier);
		 
		 processLMClassifier = null; // 如果用户使用boundary n-gram模型训练，processLMClassifier失效，释放内存
	}

	/**
	 * 通过样本训练模型,使用基于词的N-Gram模型
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	public void trainByTokenizedNGram(TokenizerFactory tokenizerFactory) throws ClassNotFoundException, IOException{// TODO
		tokenizedLMClassifier = DynamicLMClassifier.createTokenized(
				categorys.toArray(new String[categorys.size()]), tokenizerFactory, ngram);
		for(Classified<CharSequence> classified :classifiedlist){
			tokenizedLMClassifier.handle(classified);
		}
		
		compiledClassifier = (JointClassifier<CharSequence>) AbstractExternalizable.compile(tokenizedLMClassifier);
	}
	
	public JointClassification classify(String text){
		if(compiledClassifier == null){
			throw new NullPointerException("模型没有训练，请先进行训练！");
		}
		return compiledClassifier.classify(text);
	}
	
	/**
	 * 预测某文本所属的类别
	 * @param text
	 * @return
	 */
	public String predictCatogery(String text){
		if(compiledClassifier == null){
			throw new NullPointerException("模型没有训练，请先进行训练！");
		}
		return compiledClassifier.classify(text).bestCategory();
	}
	
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		LMTextClassifier classifier = new LMTextClassifier();
		
		// 添加训练样本
		classifier.addSample("习近平访津巴布韦见穆加贝：中国永不忘老朋友漫评", "政治");
		classifier.addSample("中央空降或异地调任 他们为何能当省长", "政治");
		classifier.addSample("新疆纪委书记：确有个别干部反分裂态度暧昧", "政治");
		classifier.addSample("房贷利息抵扣个税传言渐起 短期难出台", "经济");
		classifier.addSample("中央经济工作会议前瞻：宽财政稳货币改供给是主基调", "经济");
		classifier.addSample("林采宜：中国经济进入通缩时代", "经济");
		classifier.addSample("维罗尼卡：当代艺术必须要触及普通人的生活", "文化");
		classifier.addSample("虎彩搜书院上线：纸书不死，文化永存", "文化");
		classifier.addSample("各地非物质文化遗产研究类、建筑文化研究类、地区方言研究类图书", "文化");
		
		// 训练模型
//		classifier.trainByProcessNGram();
		classifier.trainByBoundaryNGram();
		
		//分词的结果，对基于词的N元分类具有较大的影响，这使得，在一定程度上，降低了基于词的N元分类。
//		classifier.setNGramSize(2);
//		classifier.trainByTokenizedNGram(new ChineseTokenizerFactory());
	
		// 预测新文本的类别
		String catogery = classifier.predictCatogery("文化产业的发展，对中国经济具有巨大的贡献");
		System.out.println(catogery);
	}
}

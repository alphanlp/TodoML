package inteldt.TodoML.classifier;

import inteldt.TodoML.tokenizer.ChineseTokenizerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.LogisticRegressionClassifier;
import com.aliasi.corpus.ListCorpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.io.Reporter;
import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;
import com.aliasi.tokenizer.TokenFeatureExtractor;
import com.aliasi.util.FeatureExtractor;

/**
 * 基于Logistic Regression的多类别文本分类
 * 
 * 调用Lingpipe的方法
 * 
 * @author shisi
 * @version 1.0   2015/12/11
 */
public class LRTextClassifier {
	private List<String> categorys; // 类别set
	private ListCorpus<Classified<CharSequence>> corpus;
	private LogisticRegressionClassifier<CharSequence> logisticClassifier;
	
	public LRTextClassifier(){
		categorys = new ArrayList<String>();
		corpus = new ListCorpus<Classified<CharSequence>>();
	}

	/**
	 * 添加样本
	 */
	public void addSample(String text, String category){
		if(!categorys.contains(category)){
			categorys.add(category); // 如新加的样本，类别集中没有，则添加
		}
		
	   Classification classification = new Classification(category); 
	   corpus.addTrain(new Classified<CharSequence>(text,classification));
	}
	
	public void defaultTrain() throws IOException{
		FeatureExtractor<CharSequence> featureExtractor
        = new TokenFeatureExtractor(new ChineseTokenizerFactory());
		
		 int minFeatureCount = 10; // 特征频次
	     boolean addInterceptFeature = true; 
	     boolean noninformativeIntercept = true;
	     RegressionPrior prior = RegressionPrior.gaussian(1.0,noninformativeIntercept); // 先验
	     
	     AnnealingSchedule annealingSchedule
         = AnnealingSchedule.exponential(0.00025,0.999);
	
	     double minImprovement = 0.000000001;
         int minEpochs = 100;
         int maxEpochs = 20000;
    	 int blockSize = corpus.trainCases().size();
    	 int rollingAvgSize = 10;
    	 
    	 train(featureExtractor,
				minFeatureCount,
				addInterceptFeature,
				prior,
				blockSize,
				null,
				annealingSchedule,
				minImprovement,
				rollingAvgSize,
				minEpochs,
				maxEpochs,
				null,
				null);
	}
	
	public void train(FeatureExtractor<CharSequence> featureExtractor,
            int minFeatureCount,
            boolean addInterceptFeature,
            RegressionPrior prior,
            int blockSize,
            LogisticRegressionClassifier<CharSequence> hotStart,
            AnnealingSchedule annealingSchedule,
            double minImprovement,
            int rollingAverageSize,
            int minEpochs,
            int maxEpochs,
            ObjectHandler<LogisticRegressionClassifier<CharSequence>> classifierHandler,
            Reporter reporter) throws IOException{
		
			logisticClassifier = LogisticRegressionClassifier.<CharSequence>train(corpus,featureExtractor,
                                                           minFeatureCount,
                                                           addInterceptFeature,
                                                           prior,
                                                           blockSize,
                                                           hotStart,
                                                           annealingSchedule,
                                                           minImprovement,
                                                           rollingAverageSize,
                                                           minEpochs,
                                                           maxEpochs,
                                                           classifierHandler,
                                                           reporter);
	}
	
	/**
	 * 预测某文本所属的类别
	 * @param text
	 * @return
	 */
	public String predictCatogery(String text){
		if(logisticClassifier == null){
			throw new NullPointerException("模型没有训练，请先进行训练！");
		}
		return logisticClassifier.classify(text).bestCategory();
	}
	
	public static void main(String[] args) throws IOException {
		LRTextClassifier classifier = new LRTextClassifier();
				
		classifier.addSample("习近平访津巴布韦见穆加贝：中国永不忘老朋友漫评", "政治");
		classifier.addSample("中央空降或异地调任 他们为何能当省长", "政治");
		classifier.addSample("新疆纪委书记：确有个别干部反分裂态度暧昧", "政治");
		classifier.addSample("房贷利息抵扣个税传言渐起 短期难出台", "经济");
		classifier.addSample("中央经济工作会议前瞻：宽财政稳货币改供给是主基调", "经济");
		classifier.addSample("林采宜：中国经济进入通缩时代", "经济");
		classifier.addSample("维罗尼卡：当代艺术必须要触及普通人的生活", "文化");
		classifier.addSample("虎彩搜书院上线：纸书不死，文化永存", "文化");
		classifier.addSample("各地非物质文化遗产研究类、建筑文化研究类、地区方言研究类图书", "文化");
		
		classifier.defaultTrain();
		
		String cat = classifier.predictCatogery("文化");
		System.out.println(cat);
		
	}
}

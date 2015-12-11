package inteldt.TodoML.tokenizer;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.io.StringReader;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;

public class ChineseTokenizerFactory implements Serializable, TokenizerFactory{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ChineseTokenizerFactory(){
	}

	public Tokenizer tokenizer(char[] ch, int start, int length) {
		return new ChineseTokenizer(ch,start,length);
	}

    Object writeReplace() {
        return new Externalizer(this);
    }

    /**
     * Return a description of this regex-based tokenizer
     * factory including its pattern's regular expression
     * and flags.
     *
     * @return A description of this regex-based tokenizer
     * factory.
     */
    @Override
    public String toString() {
        return getClass().toString();
    }

    static class Externalizer extends AbstractExternalizable {
        private static final long serialVersionUID = 7772106464245966975L;
        final ChineseTokenizerFactory mFactory;
        public Externalizer() { this(null); }
        
        public Externalizer(ChineseTokenizerFactory factory) {
            mFactory = factory;
        }
        
        @Override
        public Object read(ObjectInput in)  throws ClassNotFoundException, IOException {
            return new ChineseTokenizerFactory();
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
        	objOut.writeObject(null);
        }
    }

    static class ChineseTokenizer extends Tokenizer {
        final IKSegmenter aSeg;
        
        ChineseTokenizer(char[] cs, int start, int length) {
        	aSeg = new IKSegmenter(new StringReader(new String(cs, start, length)),true) ;
        }
        @Override
        public String nextToken() {
            try {
            	Lexeme lexeme = null;
            	if((lexeme = aSeg.next()) != null){
            		return lexeme.lexemeText();
            	}
			} catch (IOException e) {
				e.printStackTrace();
			}
            return null;
        }
        
        @Override
        public String nextWhitespace() {
            return new String(" ");
        }
       
    }

	

	
	

}

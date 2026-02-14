package logic;

import java.util.*;
import java.util.stream.Collectors;

import model.Answer;
import model.Answers;
import model.Question;
import model.Questions;

public class SearchFunction {
	private final Questions questions;
    private final Answers answers;
    
    public SearchFunction(Questions questions, Answers answers) {
    	this.questions = questions;
    	this.answers = answers;
    }
    
//    public List<Question> searchQuestions(String keyword, String author, boolean solvedQs) {
//    	List<Question> result = StatusData.databaseHelper.loadAllQs();
//    	
//    	if (solvedQs) {
//			result = questions.search(null, null, true);
//		}
//    	
//		if (!solvedQs) {
//			result = questions.search(null, null, false);
//		}
//    	
//    	if (keyword != null && !keyword.isBlank()) {
//    		result = questions.search(keyword, null, null);
//    	}
//    	
//    	if (solvedQs != null && !solvedQs.isBlank()) {
//    		String a = solvedQs.toLowerCase();
//    		result = result.stream()
//    				.filter(q -> q.getAuthor().toLowerCase().contains(a))
//    				.collect(Collectors.toList());
//    	}
//    	return result;
//    }
    
    public List<Answer> searchAnswers(String keyword, String author, Boolean isSolution) {
    	return answers.search(keyword, author, isSolution);
    }
   
}


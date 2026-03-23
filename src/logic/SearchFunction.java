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
    
    
    public List<Answer> searchAnswers(String keyword, String author, Boolean isSolution) {
    	return answers.search(keyword, author, isSolution);
    }
   
}


package com.coxphysics.terrapins.vendored.thunderstorm.thresholding;

import com.coxphysics.terrapins.vendored.thunderstorm.FormulaParser.FormulaParser;
import com.coxphysics.terrapins.vendored.thunderstorm.FormulaParser.FormulaParserException;
import com.coxphysics.terrapins.vendored.thunderstorm.FormulaParser.SyntaxTree.Node;
import com.coxphysics.terrapins.vendored.thunderstorm.FormulaParser.SyntaxTree.RetVal;

class ThresholdInterpreter {
    
    private Node tree;
    
    public ThresholdInterpreter(String formula) throws FormulaParserException {
        tree = new FormulaParser(formula, FormulaParser.FORMULA_THRESHOLD).parse();
        tree.semanticScan();
    }
    
    public float evaluate() throws FormulaParserException {
        RetVal retval = tree.eval(null);
        if(!retval.isValue())
            throw new FormulaParserException("Semantic error: result of threshold formula must be a scalar value!");
        return ((Number)(retval.get())).floatValue();
    }

}

package org.superchat.server.common.utils;


import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.ExpressionParser;

import java.lang.reflect.Method;
import java.util.Optional;

public class SpELUtil {
    private static final ExpressionParser parser=new SpelExpressionParser();
    private static final DefaultParameterNameDiscoverer parameterNameDiscovery=new DefaultParameterNameDiscoverer();

    public static String parseSpEL(Method method,Object[] args,String sqEL)
    {
        //获取参数名字
        String[] params= Optional.ofNullable(parameterNameDiscovery.getParameterNames(method)).orElse(new String[0]);
        //上下文
        EvaluationContext ctx=new StandardEvaluationContext();
        for(int i=0;i<params.length;i++)
        {
            ctx.setVariable(params[i],args[i]);
        }
        Expression expression= parser.parseExpression(sqEL);
        return expression.getValue(ctx,String.class);
    }
    public static String getMethodKey(Method method)
    {
        return method.getDeclaringClass()+"#"+method.getName();
    }
}

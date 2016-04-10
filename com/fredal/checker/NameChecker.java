package com.fredal.checker;



import java.util.EnumSet;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner7;
import javax.tools.Diagnostic.Kind;

import org.omg.CORBA.PUBLIC_MEMBER;

public class NameChecker {
	private final Messager messager;
	NameCheckScanner nameCheckScanner=new NameCheckScanner();

	NameChecker(ProcessingEnvironment processingEnv) {
		// TODO Auto-generated constructor stub
		this.messager=processingEnv.getMessager();
	}

	public void checkNames(Element element) {
		// TODO Auto-generated method stub
		nameCheckScanner.scan(element);
	}
	
	private class NameCheckScanner extends ElementScanner7<Void, Void>{
		@Override
		//检查java类
		public Void visitType(TypeElement e, Void p) {
			// TODO Auto-generated method stub
			scan(e.getTypeParameters(), p);
			checkCamelCase(e,true);
			super.visitType(e, p);
			return null;
		}
		
		//检查方法命名
		@Override
		public Void visitExecutable(ExecutableElement e, Void p) {
			// TODO Auto-generated method stub
			if(e.getKind()==ElementKind.METHOD){
				Name name=e.getSimpleName();
				if(name.contentEquals(e.getEnclosingElement().getSimpleName()))
					messager.printMessage(Kind.WARNING, "一个普通方法\""+name+"\"不能与类名重名", e);
				checkCamelCase(e, false);
			}
			 super.visitExecutable(e, p);
			 return null;
		}
		
		@Override
		public Void visitVariable(VariableElement e, Void p) {
			// TODO Auto-generated method stub
			if(e.getKind()==ElementKind.ENUM_CONSTANT||e.getConstantValue()!=null){
				checkAllCaps(e);
			}else
				checkCamelCase(e, false);
			 return null;
		}
		
		private boolean heuristicallyConstant(VariableElement e){
			if(e.getEnclosingElement().getKind()==ElementKind.INTERFACE)
				return true;
			else if(e.getKind()==ElementKind.FIELD)
				return true;
			else
				return false;
		}

		//检查是否符合驼峰命名
		private void checkCamelCase(Element e, boolean initialCaps) {
			// TODO Auto-generated method stub
			String name=e.getSimpleName().toString();
			boolean previousUpper=false;
			boolean conventional=true;
			int firstCodePoint=name.codePointAt(0);
			if(Character.isUpperCase(firstCodePoint)){
				previousUpper=true;
				if(!initialCaps){
					messager.printMessage(Kind.WARNING,"名称\""+name+"\"应当以小写字母开头",e);
					return;
				}
			}else if(Character.isLowerCase(firstCodePoint)){
				if(initialCaps){
					messager.printMessage(Kind.WARNING, "名称\""+name+"\"应当以大写字母开头", e);
					return;
				}
			}else
				conventional=false;
			
			if(conventional){
				int cp=firstCodePoint;
				for(int i=Character.charCount(cp);i<name.length();i+=Character.charCount(cp)){
					cp=name.codePointAt(i);
					if(Character.isUpperCase(cp)){
						if(previousUpper){
							conventional=false;
							break;
						}
						previousUpper=true;
					}else{
						previousUpper=false;
					}
				}
				
				if(!conventional){
					messager.printMessage(Kind.WARNING, "名称\""+name+"\"应符合驼峰规则", e);
				}
			}
		}
		
		//大写命名检查
		private void checkAllCaps(Element e){
			String name=e.getSimpleName().toString();
			boolean conventional=true;
			int firstCodePoint=name.codePointAt(0);
			
			if(!Character.isUpperCase(firstCodePoint))
				conventional=false;
			else{
				boolean previousUnderscore=false;
				int cp=firstCodePoint;
				for(int i=Character.charCount(cp);i<name.length();i+=Character.charCount(cp)){
					cp=name.codePointAt(i);
					if(cp==(int)'_'){
						if(previousUnderscore){
							conventional=false;
							break;
						}
						previousUnderscore=true;
					}else{
						previousUnderscore=false;
						if(!Character.isUpperCase(cp)&&!Character.isDigit(cp)){
							conventional=false;
							break;
						}
					}
				}
			}
			
			if(!conventional)
				messager.printMessage(Kind.WARNING, "名称\""+name+"\"应全部大写或者下划线命名,以字母开头", e);
		}
	}

}

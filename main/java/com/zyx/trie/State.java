package com.zyx.trie;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Ervin.zhang
 */
public class State {

	private int id = 0;
	
    /** 字母在单词中的深度 */
    private final int depth;

    /** 根状态，找不到匹配时，回到根节点 */
    private final State rootState;

    /** goto函数表 */
    private Map<Character,State> success = new HashMap<Character, State>();

    private State failure = null;

    private String emit = null;
    
    private Object data = null;
    
    private Set<State> beFailuredBys = new HashSet<State>();
    
    private Character absorb = null;
    
    public State() {
        this(0, 0);
    }

    public State(int id, int depth) {
    	this.id = id;
        this.depth = depth;
        this.rootState = depth == 0 ? this : null;
    }

    public State nextState(Character character, boolean ignoreRootState) {
        State nextState = this.success.get(character);
        if (!ignoreRootState && nextState == null && this.rootState != null) {
            nextState = this.rootState;
        }
        return nextState;
    }

    public State nextState(Character character) {
        return nextState(character, false);
    }

    public Character getAbsorb() {
		return absorb;
	}

	public void setAbsorb(Character absorb) {
		this.absorb = absorb;
	}

	public int getId() {
		return id;
	}


    public int getDepth() {
        return this.depth;
    }

    public void setEmit(String emit)
    {
    	this.emit = emit;
    }
    
    public void setEmit(String emit, Object data)
    {
    	this.emit = emit;
    	this.data = data;
    }
    
    public String getEmit()
    {
    	return this.emit;
    }
    
    
    public Object getData() {
		return data;
	}

	public Map<Character, State> getSuccess() {
		return success;
	}


    public State failure() {
        return this.failure;
    }

    public void setFailure(State failState) {
        
    	this.failure = failState;
        if(!failState.beFailuredBys.contains(this))
        	failState.beFailuredBys.add(this); 
    }
    
    public void removeFailure()
    {
    	if(failure != null)
    	{
    		failure.beFailuredBys.remove(this);
    		failure = null;
    	}
    }
    

    public Collection<State> getStates() {
        return this.success.values();
    }

    public Collection<Character> getTransitions() {
        return this.success.keySet();
    }
    
    public void printTree()
    {
    	System.out.println(toString());
    	for(State s : getStates())
    		s.printTree();
    }
    
    
    
    public Set<State> getBeFailuredBys() {
		return beFailuredBys;
	}

	public String toString()
    {
    	String go = "{";
    	for(Entry<Character,State> e : success.entrySet())
    		go += e.getKey() + "->" + e.getValue().getId() + ", ";
    	go += "}";
    	
    	String befailuredby = "";
    	
    	for(State s : this.beFailuredBys)
    		befailuredby += s.getId() + ",";
    	
    	String tab = "";
    	
    	for(int i = 0 ; i < depth; ++ i)
    		tab += "\t";
    	
    	String txt = tab  + "{id=" + id + ", absorb=" + absorb + ",go=" + go + ", failure=" + (failure != null? failure.getId(): "null") + ", befailuredby=[" + befailuredby + "], emit=" + emit + ", data=" + data + "}";
    	return txt;
    }

}

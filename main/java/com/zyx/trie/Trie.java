package com.zyx.trie;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.zyx.filter.text.Behavior;
import com.zyx.filter.text.ParseResult;
import com.zyx.filter.thrift.TFilterLevel;

/**
 * AC多模式匹配的java实现 基于 << Efficient String Matching: An Aid to Bibliographic
 * Search.pdf >>
 * 
 * @author Ervin.zhang
 */
public class Trie {
	
	/** 根节点状态 */
	private State rootState;
	/** 当前最大id */
	private int maxId = 0;
	/** 区分大小写 */
	private boolean caseInsensitive;
	private boolean failureStatesConstructed = false;

	public Trie(boolean caseInsensitive) {
		this.caseInsensitive = caseInsensitive;
		this.rootState = new State(maxId++, 0);
	}

	public Trie() {
		this(false);
	}

	public Trie caseInsensitive() {
		this.caseInsensitive = true;
		return this;
	}

	/**
	 * 增加关键字并构建goto函数
	 * 
	 * @param keyword
	 *            关键字
	 * @param data
	 *            输出内容
	 */
	public void addKeyword(String keyword, Object data) {
		if (keyword == null || keyword.length() == 0) {
			return;
		}

		if (caseInsensitive) {
			keyword = keyword.toLowerCase();
		}

		State currentState = this.rootState;
		for (Character character : keyword.toCharArray()) {
			currentState = addState(currentState, character);
		}
		currentState.setEmit(keyword, data);
	}

	public void addKeyword(String keyword) {
		addKeyword(keyword, null);
	}

	public State addState(State state, Character character) {
		State nextState = state.getSuccess().get(character);
		if (nextState == null) {
			nextState = new State(maxId++, state.getDepth() + 1);
			state.getSuccess().put(character, nextState);
			nextState.setAbsorb(character);
		}
		return nextState;
	}

	/**
	 * 增加关键字不重建特里树
	 */
	public void addKeywordInc(String keyword, Object data) {
		if (!this.failureStatesConstructed) {
			addKeyword(keyword, data);
			return;
		}

		if (keyword == null || keyword.length() == 0) {
			return;
		}
		if (caseInsensitive) {
			keyword = keyword.toLowerCase();
		}

		State currentState = this.rootState;
		State nextState = null;
		char[] chars = keyword.toCharArray();
		int pos = 0;
		for (; pos < chars.length; ++pos) {
			Character character = chars[pos];
			nextState = currentState.nextState(character, true);
			if (nextState == null)
				break;
			currentState = nextState;
		}

		if (pos >= chars.length) // existing path, not need to add new state,
									// just add emit
		{
			if (!keyword.equals(currentState.getEmit()))
				currentState.setEmit(keyword, data);
		} else {
			State state = currentState;
			for (; pos < chars.length; ++pos) {
				Character character = chars[pos];
				state = addState(state, character);
			}
			state.setEmit(keyword, data);

			if (currentState == this.rootState) {
				this.constructFailureStates();
			} else {
				Queue<State> queue = new LinkedList<State>();
				queue.add(currentState);
				queue.addAll(currentState.getBeFailuredBys());

				buildFailure(queue);
			}
		}
	}

	public void addKeywordInc(String keyword) {
		addKeywordInc(keyword, null);
	}

	public void removeKeywordInc(String keyword) {
		if (keyword == null || keyword.length() == 0) {
			return;
		}
		if (caseInsensitive) {
			keyword = keyword.toLowerCase();
		}

		State currentState = this.rootState;
		State nextState = null;
		char[] chars = keyword.toCharArray();
		int pos = 0;

		List<State> list = new ArrayList<State>();

		for (; pos < chars.length; ++pos) {
			list.add(currentState);
			Character character = chars[pos];
			nextState = currentState.nextState(character, true);
			if (nextState == null)
				break;
			currentState = nextState;
		}

		if (pos >= chars.length)// keyword path match in the goto tree
		{
			currentState.setEmit(null);

			if (currentState.getSuccess().size() > 0) // no need to prune the
														// tree, just return
				return;

			list.add(currentState);

			pruneFailure(list);
		} else {
		} // keyword path not found, just ignore

	}

	private void pruneFailure(List<State> list) {
		for (int i = list.size() - 1; i > 0; --i) {
			State state = list.get(i);

			State prevState = list.get(i - 1);

			if (state.getEmit() == null && state.getSuccess().isEmpty()) {
				for (State by : state.getBeFailuredBys())
					// transfer failure function
					by.setFailure(state.failure());

				State stateFailure = state.failure();

				if (stateFailure != null)
					stateFailure.getBeFailuredBys().remove(state);

				prevState.getSuccess().remove(state.getAbsorb()); // remove
																	// transition
																	// from pre
																	// to state

				state = null;
			} else {
				break;
			}
		}
	}

	/**
	 * 文本过滤
	 * @param text 被过滤广西
	 * @return 过滤结果
	 * */
	public ParseResult filterText(String text) {
		checkBuild();

		if (caseInsensitive) {
			text = text.toLowerCase();
		}

		State currentState = this.rootState;

		ParseResult result = new ParseResult();
		StringBuilder filteredText = new StringBuilder();

		for (Character character : text.toCharArray()) {
			filteredText.append(character);
			currentState = getState(currentState, character);
			String emit = currentState.getEmit();
			if (emit != null) {
				// 节点数据放的是Behavior
				Behavior behavior = (Behavior) currentState.getData();
				TFilterLevel level = behavior.action(filteredText);
				if (result.getLevel() == null) {
					result.setLevel(level);
				} else {
					// 敏感级别只能增加
					if (level.ordinal() > result.getLevel().ordinal()) {
						result.setLevel(level);
					}

				}
				if (level == TFilterLevel.FORBIDDEN) {
					result.setFilteredText(filteredText.toString());
					// 如果出现了禁止的内容,直接返回
					return result;
				}
			}
		}

		result.setFilteredText(filteredText.toString());
		return result;
	}

	private State getState(State currentState, Character character) {
		State newCurrentState = currentState.nextState(character);
		while (newCurrentState == null) {
			currentState = currentState.failure();
			newCurrentState = currentState.nextState(character);
		}
		return newCurrentState;
	}

	public void checkBuild() {
		if (!this.failureStatesConstructed) {
			constructFailureStates();
			this.failureStatesConstructed = true;
		}
	}
	/**
	 * 构建failure函数
	 */
	private void constructFailureStates() {
		Queue<State> queue = new LinkedList<State>();

		// 设置深度为1节点的failure跳转
		for (State depthOneState : this.rootState.getStates()) {
			depthOneState.setFailure(this.rootState);
			queue.offer(depthOneState);
		}

		// 设置 深度 > 1 节点的failure跳转
		while (!queue.isEmpty()) {

			State currentState = queue.poll();

			for (Character transition : currentState.getTransitions()) {
				State targetState = currentState.nextState(transition);

				queue.offer(targetState);

				State traceFailureState = currentState.failure();

				State traceFailureNextState = traceFailureState
						.nextState(transition);

				while (traceFailureNextState == null) {
					traceFailureState = traceFailureState.failure();
					traceFailureNextState = traceFailureState
							.nextState(transition);
				}
				targetState.setFailure(traceFailureNextState);
			}
		}
	}

	private void buildFailure(Queue<State> queue) {
		while (!queue.isEmpty()) {

			State currentState = queue.poll();

			for (Character transition : currentState.getTransitions()) {
				State targetState = currentState.nextState(transition);

				queue.offer(targetState);

				State traceFailureState = currentState.failure();

				State traceFailureNextState = traceFailureState
						.nextState(transition);

				while (traceFailureNextState == null) {
					traceFailureState = traceFailureState.failure();
					traceFailureNextState = traceFailureState
							.nextState(transition);
				}
				targetState.removeFailure();
				targetState.setFailure(traceFailureNextState);
			}
		}
	}

	public void printTrie() {
		this.rootState.printTree();
	}
}

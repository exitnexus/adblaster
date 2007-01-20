/*
 * Copyright (c) <2006> <Thomas Roy, modified from Dennis Sosnoski's code>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.nexopia.adblaster.util;



/**
 * Hash map using primitive <code>int</code> values as keys mapped to
 * <code>String</code> values. This implementation is unsynchronized in order
 * to provide the best possible performance for typical usage scenarios, so
 * explicit synchronization must be implemented by a wrapper class or directly
 * by the application in cases where instances are modified in a multithreaded
 * environment. See the bacopyright holdersse classes for other details of the hash map
 * implementation.
 * 
 * @author Dennis M. Sosnoski
 * @version 1.1
 */

public class IntObjectHashMap<Value> extends PrimitiveKeyBase {
	/** Array of key table slots. */
	protected int[] m_keyTable;

	/** Array of value table slots. */
	protected Value[] m_valueTable;

	/**
	 * Constructor with full specification.
	 * 
	 * @param count
	 *            number of values to assume in initial sizing of table
	 * @param fill
	 *            fraction full allowed for table before growing
	 */

	public IntObjectHashMap(int count, double fill) {
		super(count, fill, int.class, Object.class);
	}

	/**
	 * Constructor with only size supplied. Uses default value for fill
	 * fraction.
	 * 
	 * @param count
	 *            number of values to assume in initial sizing of table
	 */

	public IntObjectHashMap(int count) {
		this(count, DEFAULT_FILL);
	}

	/**
	 * Default constructor.
	 */

	public IntObjectHashMap() {
		this(0, DEFAULT_FILL);
	}

	/**
	 * Copy (clone) constructor.
	 * 
	 * @param base
	 *            instance being copied
	 */

	public IntObjectHashMap(IntObjectHashMap<Value> base) {
		super(base);
	}

	/**
	 * This function returns an array full of keys and non-keys. Non-keys
	 * are initialized to 0 by default, and 0 may also be an acceptable key,
	 * so be aware that there's no way to determine whether a 0 entry is a 
	 * key or not without using the m_flagTable.
	 * 
	 * Get the backing array of keys. This implementation of an abstract method
	 * is used by the type-agnostic base class code to access the array used for
	 * type-specific storage by the child class.
	 * 
	 * @return backing key array object
	 */

	public final int[] getKeyArray() {
		return m_keyTable;
	}

	/**
	 * Set the backing array of keys. This implementation of an abstract method
	 * is used by the type-agnostic base class code to set the array used for
	 * type-specific storage by the child class.
	 * 
	 * @param array
	 *            backing key array object
	 */

	protected final void setKeyArray(int[] array) {
		m_keyTable = array;
	}

	/**
	 * Get the backing array of values. This implementation of an abstract
	 * method is used by the type-agnostic base class code to access the array
	 * used for type-specific storage by the child class.
	 * 
	 * @return backing key array object
	 */

	protected final Value[] getValueArray() {
		return m_valueTable;
	}

	/**
	 * Set the backing array of values. This implementation of an abstract
	 * method is used by the type-agnostic base class code to set the array used
	 * for type-specific storage by the child class.
	 * 
	 * @param array
	 *            backing value array object
	 */

	protected final void setValueArray(Value[] array) {
		m_valueTable = array;
	}

	/**
	 * Reinsert an entry into the hash map. This method is designed for internal
	 * use when the table is being modified, and does not adjust the count
	 * present or check the table capacity.
	 * 
	 * @param slot
	 *            position of entry to be reinserted into hash map
	 * @return <code>true</code> if the slot number used by the entry has has
	 *         changed, <code>false</code> if not
	 */

	protected final boolean reinsert(int slot) {
		m_flagTable[slot] = false;
		return assignSlot(m_keyTable[slot], m_valueTable[slot]) != slot;
	}

	/**
	 * Restructure the table. This implementation of an abstract method is used
	 * when the table is increasing or decreasing in size, and works directly
	 * with the old table representation arrays. It inserts pairs from the old
	 * arrays directly into the table without adjusting the count present or
	 * checking the table size.
	 * 
	 * @param flags
	 *            array of flags for array slots used
	 * @param karray
	 *            array of keys
	 * @param varray
	 *            array of values
	 */

	protected void restructure(boolean[] flags, int[] karray, Value[] varray) {
		int[] keys = (int[]) karray;
		Value[] values = (Value[]) varray;
		for (int i = 0; i < flags.length; i++) {
			if (flags[i]) {
				assignSlot(keys[i], values[i]);
			}
		}
	}

	/**
	 * Compute the base slot for a key.
	 * 
	 * @param key
	 *            key value to be computed
	 * @return base slot for key
	 */

	protected final int computeSlot(int key) {
		return (key * KEY_MULTIPLIER & Integer.MAX_VALUE) % m_flagTable.length;
	}

	/**
	 * Assign slot for entry. Starts at the slot found by the hashed key value.
	 * If this slot is already occupied, it steps the slot number and checks the
	 * resulting slot, repeating until an unused slot is found. This method does
	 * not check for duplicate keys, so it should only be used for internal
	 * reordering of the tables.
	 * 
	 * @param key
	 *            key to be added to table
	 * @param value
	 *            associated value for key
	 * @return slot at which entry was added
	 */

	protected int assignSlot(int key, Value value) {
		int offset = freeSlot(computeSlot(key));
		m_flagTable[offset] = true;
		m_keyTable[offset] = key;
		m_valueTable[offset] = value;
		return offset;
	}

	/**
	 * Add an entry to the table. If the key is already present in the table,
	 * this replaces the existing value associated with the key.
	 * 
	 * @param key
	 *            key to be added to table
	 * @param value
	 *            associated value for key
	 * @return value previously associated with key, <code>null</code> if key
	 *         not previously present in table
	 */

	public Value put(int key, Value value) {
		ensureCapacity(m_entryCount + 1);
		int offset = internalFind(key);
		if (offset >= 0) {
			Value prior = m_valueTable[offset];
			m_valueTable[offset] = value;
			return prior;
		} else {
			m_entryCount++;
			offset = -offset - 1;
			m_flagTable[offset] = true;
			m_keyTable[offset] = key;
			m_valueTable[offset] = value;
			return null;
		}
	}

	/**
	 * Internal find key in table.
	 * 
	 * @param key
	 *            to be found in table
	 * @return index of matching key, or <code>-index-1</code> of slot to be
	 *         used for inserting key in table if not already present (always
	 *         negative)
	 */

	protected final int internalFind(int key) {
		int slot = computeSlot(key);
		while (m_flagTable[slot]) {
			if (key == m_keyTable[slot]) {
				return slot;
			}
			slot = stepSlot(slot);
		}
		return -slot - 1;
	}

	/**
	 * Check if an entry is present in the table. This method is supplied to
	 * support the use of <code>null</code> values in the table.
	 * 
	 * @param key
	 *            key for entry to be found
	 * @return <code>true</code> if key found in table, <code>false</code>
	 *         if not
	 */

	public final boolean containsKey(int key) {
		return internalFind(key) >= 0;
	}

	/**
	 * Find an entry in the table.
	 * 
	 * @param key
	 *            key for entry to be returned
	 * @return value for key, or <code>null</code> if key not found
	 */

	public final Value get(int key) {
		int slot = internalFind(key);
		if (slot >= 0) {
			return m_valueTable[slot];
		} else {
			return null;
		}
	}

	/**
	 * Remove an entry from the table.
	 * 
	 * @param key
	 *            key to be removed from table
	 * @return value associated with removed key, <code>null</code> if key not
	 *         found in table
	 */

	public Value remove(int key) {
		int slot = internalFind(key);
		if (slot >= 0) {
			Value value = m_valueTable[slot];
			m_flagTable[slot] = false;
			m_entryCount--;
			while (m_flagTable[(slot = stepSlot(slot))]) {
				reinsert(slot);
			}
			return value;
		} else {
			return null;
		}
	}

	/**
	 * Return an iterator for the value <code>Object</code>s in this map. The
	 * iterator returns all values in arbitrary order, but is not "live". Any
	 * changes to the map while the iteration is in progress will give
	 * indeterminant results.
	 * 
	 * @return iterator for values in array
	 * 
	 * public final Iterator valueIterator() { return
	 * SparseArrayIterator.buildIterator(m_valueTable); }
	 */

	/**
	 * Construct a copy of the table.
	 * 
	 * @return shallow copy of table
	 */

	public IntObjectHashMap<Value> clone() {
		return new IntObjectHashMap<Value>(this);
	}

	@Override
	protected void restructure(boolean[] flags, Object karray, Object varray) {
		restructure(flags, (int[])karray, (Value[])varray);
		
	}

	@Override
	protected void setValueArray(Object array) {
		setValueArray((Value[]) array);
		
	}

	@Override
	protected void setKeyArray(Object array) {
		setKeyArray((int[])array);
		
	}
}

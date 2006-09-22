/*
 * Created on Jun 20, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import com.nexopia.adblaster.util.Integer;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.DatabaseEntry;

/**
 * @author wolfe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class IntegerBinding extends TupleBinding {

	/* (non-Javadoc)
	 * @see com.sleepycat.bind.tuple.TupleBinding#entryToObject(com.sleepycat.bind.tuple.TupleInput)
	 */
	public Object entryToObject(TupleInput ti) {
		return new Integer(ti.readInt());
	}

	public int entryToInt(DatabaseEntry entry) {
        return entryToInput(entry).readInt();
	}

	/* (non-Javadoc)
	 * @see com.sleepycat.bind.tuple.TupleBinding#objectToEntry(java.lang.Object, com.sleepycat.bind.tuple.TupleOutput)
	 */
	public void objectToEntry(Object o, TupleOutput to) {
		Integer i = (Integer)o;
		to.writeInt(i.intValue());
	}

    protected TupleOutput getTupleOutput() {
        int byteSize = getTupleBufferSize();
        if (byteSize != 0) {
            return new TupleOutput(new byte[byteSize]);
        } else {
            return new TupleOutput();
        }
    }

	public void intToEntry(int i, DatabaseEntry entry) {
        TupleOutput output = getTupleOutput();
		output.writeInt(i);
        outputToEntry(output, entry);
	}

	public void intBannerViewToEntry(int i, int j, int k, byte l, int m, DatabaseEntry entry) {
        TupleOutput output = getTupleOutput();
		output.writeInt(i);
		output.writeInt(j);
		output.writeInt(k);
		output.writeByte(l);
		output.writeInt(m);
		outputToEntry(output, entry);
	}
}

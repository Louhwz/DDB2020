/*
 * Created on 2005-5-17
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package transaction.model;

import transaction.exception.InvalidIndexException;

import java.io.Serializable;

/**
 * @author RAdmin
 * <p>
 */
public interface ResourceItem extends Cloneable, Serializable {

    String[] getColumnNames();

    String[] getColumnValues();

    Object getIndex(String indexName) throws InvalidIndexException;

    Object getKey();

    boolean isDeleted();

    void delete();

    Object clone();
}
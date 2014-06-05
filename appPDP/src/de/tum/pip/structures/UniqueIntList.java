
package de.tum.pip.structures;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The Class UniqueIntList is used to create an IntegerList with Unique
 * Elements.
 */
public class UniqueIntList extends ArrayList<Integer> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6936139453437356585L;

    // Make it Unique!!

    /*
     * (non-Javadoc)
     * @see java.util.ArrayList#add(java.lang.Object)
     */
    @Override
    public boolean add(Integer e) {
        if (!contains(e)) { // check, if the element is already contained, to
                            // ensure uniqueness
            return super.add(e);
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see java.util.ArrayList#add(int, java.lang.Object)
     */
    @Override
    public void add(int index, Integer element) {
        if (!contains(element)) { // check, if the element is already contained,
                                  // to ensure uniqueness
            super.add(index, element);
        }
    }

    /*
     * (non-Javadoc)
     * @see java.util.ArrayList#addAll(java.util.Collection)
     */
    @Override
    public boolean addAll(Collection<? extends Integer> c) {
        boolean changed = false; // addAll returns, wheather the List has
                                 // changed at
                                 // all.
        for (Integer element : c) {
            if (!contains(element)) { // we could also use the this.add
                                      // function, but the if saves us some
                                      // function calls
                super.add(element); // when we add an element the List has
                                    // changed.
                changed = true;
            }
        }
        return changed;
    }

    /*
     * (non-Javadoc)
     * @see java.util.ArrayList#addAll(int, java.util.Collection)
     */
    @Override
    public boolean addAll(int index, Collection<? extends Integer> c) {
        boolean changed = false; // addAll returns, wheather the List has
                                 // changed at
                                 // all.
        int i = index;
        for (Integer element : c) {
            if (!contains(element)) {
                // TODO: This is not very effective.
                super.add(i++, element); // when we add an element the List has
                                         // changed.
                                         // //increse i, so the next element is
                                         // added at
                                         // the next position.
                changed = true;
            }
        }
        return changed;
    }

    /*
     * (non-Javadoc)
     * @see java.util.ArrayList#set(int, java.lang.Object)
     */
    @Override
    public Integer set(int index, Integer element) {
        if (!contains(element)) { // if the element is not contained we can use
                                  // the set function
            return super.set(index, element);
        } else {
            // TODO: this may destroy the indexes of all other elements!
            Integer oldValue = remove(index); // remove and return the old
                                              // element.
            remove(element); // remove the Element
            super.add(index, element); // and add it at its new position.
            return oldValue;
        }
    }

}

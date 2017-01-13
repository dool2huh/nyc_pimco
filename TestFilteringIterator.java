import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class TestFilteringIterator {

    private static final IObjectTest<Integer> ALWAYS_TRUE_FILTER = element -> true;
    private static final IObjectTest<Integer> EVEN_NUMBER_FILTER = element -> element % 2 == 0;

    private List<Integer> numberData = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
    private List<Integer> dataWithNull = Arrays.asList(1, 2, null, 4, 5);
    private List<String> carData = Arrays.asList("mustang", "harley", "corvette", "bmw");

    @Test
    public void testNoFilter() {
        try {
            new FilteringIterator<Integer>(numberData.iterator(), null);
            Assert.fail("IllegalArgumentException expected for null filter");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testNoIterator() {
        try {
            new FilteringIterator<Integer>(null, ALWAYS_TRUE_FILTER);
            Assert.fail("IllegalArgumentException expected for null iterator");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testRemove() {
        Iterator<Integer> iterator = new FilteringIterator<Integer>(numberData.iterator(), EVEN_NUMBER_FILTER);

        try {
            iterator.remove();
            Assert.fail("Expected UnsupportedOperationException for remove()");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testGetEvenNumberFilter() {
        Iterator<Integer> iterator = new FilteringIterator<Integer>(numberData.iterator(), EVEN_NUMBER_FILTER);

        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(2, (int) iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(4, (int) iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(6, (int) iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(8, (int) iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(10, (int) iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(12, (int) iterator.next());

        Assert.assertFalse(iterator.hasNext());

        checkNoSuchElementException(iterator);
    }

    @Test
    public void testGetNumberFilterAllowingNull() {
        Iterator<Integer> iterator = new FilteringIterator<Integer>(dataWithNull.iterator(), element -> element == null || element !=2 );

        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(1, (int) iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertNull(iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(4, (int) iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(5, (int) iterator.next());

        Assert.assertFalse(iterator.hasNext());

        checkNoSuchElementException(iterator);

    }

    @Test
    public void testStringDataNext() {
        Iterator<String> iterator = new FilteringIterator<String>(carData.iterator(), element -> !"mustang".equals(element));

        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals("harley", iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals("corvette", iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals("bmw", iterator.next());

        Assert.assertFalse(iterator.hasNext());

        checkNoSuchElementException(iterator);
    }

    @Test
    public void testFilterExceptionPropagatedBack() {
        Iterator<String> iterator = new FilteringIterator<String>(carData.iterator(),
                element -> {
                    if ("harley".equals(element))
                        throw new RuntimeException("invalid car name");
                    else
                        return true;
                }
        );

        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals("mustang", iterator.next());

        try {
            iterator.hasNext();
            Assert.fail("Expected filter error to be thrown");
        } catch (FilteringIterator.FilterFailedException e) {
        }
    }

    private void checkNoSuchElementException(Iterator iterator) {
        try {
            iterator.next();
            Assert.fail("NoSuchElementException expected");
        } catch (NoSuchElementException e) {
        }
    }
}

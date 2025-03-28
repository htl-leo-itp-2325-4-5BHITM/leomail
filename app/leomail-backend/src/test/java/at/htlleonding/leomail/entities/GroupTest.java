package at.htlleonding.leomail.entities;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class GroupTest {
	@Test
	public void Group() {
		Group expected = new Group("abc", "abc", new Contact("abc", "abc", "abc"), new Project("abc", "abc", new Contact("abc", "abc", "abc")));
		Group actual = new Group();

		assertEquals(expected, actual);
	}
}

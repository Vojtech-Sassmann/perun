package cz.metacentrum.perun.core.impl.modules.attributes;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_member_attribute_def_def_o365EmailAddresses_muTest.BeanAttributeMatcher;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_group_resource_attribute_def_def_o365EmailAddresses_mu.ADNAME_ATTRIBUTE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests attribute module.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unchecked")
public class urn_perun_group_resource_attribute_def_def_o365EmailAddresses_muTest {

	private urn_perun_group_resource_attribute_def_def_o365EmailAddresses_mu classInstance;
	private PerunSessionImpl session;
	private Attribute attributeToCheck;
	private final Group group = new Group(1,"group1","Group 1",null,null,null,null,0,0);
	private final Resource resource = new Resource(2,"resource2","Resource 2",3);
	private final String adName = "aaaaa";
	private Attribute adNameAttr;
	private AttributesManagerBl am;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_group_resource_attribute_def_def_o365EmailAddresses_mu();
		//prepare mocks
		session = mock(PerunSessionImpl.class);
		PerunBl perunBl = mock(PerunBl.class);
		am = mock(AttributesManagerBl.class);
		adNameAttr = mock(Attribute.class);
		when(session.getPerunBl()).thenReturn(perunBl);
		when(perunBl.getAttributesManagerBl()).thenReturn(am);
		when(am.getAttribute(session, resource, group, ADNAME_ATTRIBUTE)).thenReturn(adNameAttr);
		when(adNameAttr.getValue()).thenReturn(adName);

		attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
		attributeToCheck.setId(100);

	}

	@Test
	public void fillAttribute() throws Exception {
		System.out.println("fillAttribute()");
		Attribute attribute = classInstance.fillAttribute(session, group, resource, classInstance.getAttributeDefinition());
		Object attributeValue = attribute.getValue();
		assertThat(attributeValue, is(notNullValue()));
		List<String> expectedValue = new ArrayList<>();
		expectedValue.add(adName + "@group.muni.cz");
		assertThat(attributeValue, equalTo(expectedValue));

		//check that value generated by fillAttribute() is acceptable for checkAttributeSemantics()
		when(am.getPerunBeanIdsForUniqueAttributeValue(eq(session), argThat(new BeanAttributeMatcher("group_resource"))))
				.thenReturn(Sets.newHashSet(new Pair<>(group.getId(),resource.getId())));
		when(am.getPerunBeanIdsForUniqueAttributeValue(eq(session), argThat(new BeanAttributeMatcher("member"))))
				.thenReturn(Sets.newHashSet());
		classInstance.checkAttributeSemantics(session, group, resource, attribute);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckEmailSyntax() throws Exception {
		System.out.println("testCheckEmailSyntax()");
		attributeToCheck.setValue(Lists.newArrayList("my@example.com", "a/-+"));
		classInstance.checkAttributeSyntax(session, group, resource, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckDuplicates() throws Exception {
		System.out.println("testCheckDuplicates()");
		attributeToCheck.setValue(Lists.newArrayList("my@example.com", "aaa@bbb.com", "my@example.com"));
		classInstance.checkAttributeSyntax(session, group, resource, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckValueExistIfAdNameSetWithNull() throws Exception {
		System.out.println("testCheckValueExistIfAdNameSetWithNull()");
		attributeToCheck.setValue(null);
		classInstance.checkAttributeSemantics(session, group, resource, attributeToCheck);
	}

	@Test
	public void testCheckNullForNullAdName() throws Exception {
		System.out.println("testCheckNullForNullAdName()");
		attributeToCheck.setValue(null);
		when(adNameAttr.getValue()).thenReturn(null);
		when(am.getPerunBeanIdsForUniqueAttributeValue(eq(session),any(Attribute.class))).thenReturn(Sets.newHashSet());
		classInstance.checkAttributeSemantics(session, group, resource, attributeToCheck);
	}


	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckValueExistIfAdNameSet() throws Exception {
		System.out.println("testCheckValueExistIfAdNameSet()");
		attributeToCheck.setValue(Lists.newArrayList());
		classInstance.checkAttributeSemantics(session, group, resource, attributeToCheck);
	}

	@Test
	public void testCorrect() throws Exception {
		System.out.println("testCorrect");
		when(adNameAttr.getValue()).thenReturn(null);
		when(am.getPerunBeanIdsForUniqueAttributeValue(eq(session),any(Attribute.class))).thenReturn(Sets.newHashSet());
		attributeToCheck.setValue(Lists.newArrayList());
		classInstance.checkAttributeSyntax(session, group, resource, attributeToCheck);
		classInstance.checkAttributeSemantics(session, group, resource, attributeToCheck);
		attributeToCheck.setValue(Lists.newArrayList("my@example.com"));
		classInstance.checkAttributeSyntax(session, group, resource, attributeToCheck);
		classInstance.checkAttributeSemantics(session, group, resource, attributeToCheck);
	}

	@Test
	public void testUniqItself() throws Exception {
		System.out.println("testUniqItself");
		attributeToCheck.setValue(new ArrayList<>(Arrays.asList("my@example.com", "my2@google.com")));
		when(am.getPerunBeanIdsForUniqueAttributeValue(eq(session), argThat(new BeanAttributeMatcher("group_resource"))))
				.thenReturn(Sets.newHashSet(new Pair<>(group.getId(),resource.getId())));
		when(am.getPerunBeanIdsForUniqueAttributeValue(eq(session), argThat(new BeanAttributeMatcher("member"))))
				.thenReturn(Sets.newHashSet());
		classInstance.checkAttributeSemantics(session, group, resource, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testUniqClash() throws Exception {
		System.out.println("testUniqClash");
		attributeToCheck.setValue(new ArrayList<>(Arrays.asList("my@example.com", "my2@google.com")));
		when(am.getPerunBeanIdsForUniqueAttributeValue(eq(session), argThat(new BeanAttributeMatcher("group_resource"))))
				.thenReturn(Sets.newHashSet(new Pair<>(1000,2000)));
		when(am.getPerunBeanIdsForUniqueAttributeValue(eq(session), argThat(new BeanAttributeMatcher("member"))))
				.thenReturn(Sets.newHashSet());
		classInstance.checkAttributeSemantics(session, group, resource, attributeToCheck);
	}
}

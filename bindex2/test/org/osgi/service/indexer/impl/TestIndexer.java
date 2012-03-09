package org.osgi.service.indexer.impl;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.mockito.ArgumentCaptor;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.indexer.Capability;
import org.osgi.service.indexer.Requirement;
import org.osgi.service.indexer.Resource;
import org.osgi.service.indexer.ResourceAnalyzer;
import org.osgi.service.indexer.ResourceIndexer;
import org.osgi.service.log.LogService;

public class TestIndexer extends TestCase {
	
	public void testFragmentBsnVersion() throws Exception {
		assertFragmentMatch("testdata/fragment-01.txt", "testdata/01-bsn+version.jar");
	}
	
	public void testFragmentLocalization() throws Exception {
		assertFragmentMatch("testdata/fragment-02.txt", "testdata/02-localization.jar");
	}
	
	public void testFragmentExport() throws Exception {
		assertFragmentMatch("testdata/fragment-03.txt", "testdata/03-export.jar");
	}
	
	public void testFragmentExportWithUses() throws Exception {
		assertFragmentMatch("testdata/fragment-04.txt", "testdata/04-export+uses.jar");
	}
	
	public void testFragmentImport() throws Exception {
		assertFragmentMatch("testdata/fragment-05.txt", "testdata/05-import.jar");
	}
	
	public void testFragmentRequireBundle() throws Exception {
		assertFragmentMatch("testdata/fragment-06.txt", "testdata/06-requirebundle.jar");
	}
	
	public void testFragmentOptionalImport() throws Exception {
		assertFragmentMatch("testdata/fragment-07.txt", "testdata/07-optionalimport.jar");
	}
	
	public void testFragmentFragmentHost() throws Exception {
		assertFragmentMatch("testdata/fragment-08.txt", "testdata/08-fragmenthost.jar");
	}
	
	public void testFragmentSingletonBundle() throws Exception {
		assertFragmentMatch("testdata/fragment-09.txt", "testdata/09-singleton.jar");
	}
	
	public void testFragmentExportService() throws Exception {
		assertFragmentMatch("testdata/fragment-10.txt", "testdata/10-exportservice.jar");
	}

	public void testFragmentImportService() throws Exception {
		assertFragmentMatch("testdata/fragment-11.txt", "testdata/11-importservice.jar");
	}
	
	public void testFragmentForbidFragments() throws Exception {
		assertFragmentMatch("testdata/fragment-12.txt", "testdata/12-nofragments.jar");
	}
	
	public void testFragmentBREE() throws Exception {
		assertFragmentMatch("testdata/fragment-13.txt", "testdata/13-bree.jar");
	}
	
	public void testFragmentProvideRequireCap() throws Exception {
		assertFragmentMatch("testdata/fragment-14.txt", "testdata/14-provide-require-cap.jar");
	}

	private static void assertFragmentMatch(String expectedPath, String jarPath) throws Exception {
		BIndex2 indexer = new BIndex2();
		
		StringWriter writer = new StringWriter();
		indexer.indexFragment(Collections.singleton(new File(jarPath)), writer, null);
		
		String expected = Utils.readStream(new FileInputStream(expectedPath));
		assertEquals(expected, writer.toString().trim());
	}
	
	public void testFullIndex() throws Exception {
		BIndex2 indexer = new BIndex2();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Set<File> files = new LinkedHashSet<File>();
		files.add(new File("testdata/03-export.jar"));
		files.add(new File("testdata/06-requirebundle.jar"));
		
		Map<String, String> config = new HashMap<String, String>();
		config.put(BIndex2.REPOSITORY_INCREMENT_OVERRIDE, "0");
		config.put(ResourceIndexer.REPOSITORY_NAME, "full-c+f");
		indexer.index(files, out, config);
		
		String expected = Utils.readStream(new FileInputStream("testdata/full-03+06.txt"));
		assertEquals(expected, out.toString());
	}
	
	public void testFullIndexCompressed() throws Exception {
		BIndex2 indexer = new BIndex2();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Set<File> files = new LinkedHashSet<File>();
		files.add(new File("testdata/03-export.jar"));
		files.add(new File("testdata/06-requirebundle.jar"));
		
		Map<String, String> config = new HashMap<String, String>();
		config.put(BIndex2.REPOSITORY_INCREMENT_OVERRIDE, "0");
		config.put(ResourceIndexer.COMPRESS, Boolean.toString(true));
		config.put(ResourceIndexer.REPOSITORY_NAME, "full-c+f");
		indexer.index(files, out, config);
		
		String expected = Utils.readStream(new FileInputStream("testdata/packed.txt"));
		assertEquals(expected, Utils.decompress(out.toString()));
	}
	
	
	public void testAddAnalyzer() throws Exception {
		BIndex2 indexer = new BIndex2();
		indexer.addAnalyzer(new WibbleAnalyzer(), null);
		
		StringWriter writer = new StringWriter();
		LinkedHashSet<File> files = new LinkedHashSet<File>();
		files.add(new File("testdata/01-bsn+version.jar"));
		files.add(new File("testdata/02-localization.jar"));
		
		indexer.indexFragment(files, writer, null);
		String expected = Utils.readStream(new FileInputStream("testdata/fragment-wibble.txt"));

		assertEquals(expected, writer.toString().trim());
	}
	
	public void testAddAnalyzerWithFilter() throws Exception {
		BIndex2 indexer = new BIndex2();
		indexer.addAnalyzer(new WibbleAnalyzer(), FrameworkUtil.createFilter("(location=*sion.jar)"));
		
		StringWriter writer = new StringWriter();
		LinkedHashSet<File> files = new LinkedHashSet<File>();
		files.add(new File("testdata/01-bsn+version.jar"));
		files.add(new File("testdata/02-localization.jar"));
		
		indexer.indexFragment(files, writer, null);
		String expected = Utils.readStream(new FileInputStream("testdata/fragment-wibble-filtered.txt"));

		assertEquals(expected, writer.toString().trim());
	}
	
	public void testRootInSubdirectory() throws Exception {
		BIndex2 indexer = new BIndex2();
		
		Map<String, String> props = new HashMap<String, String>();
		props.put(ResourceIndexer.ROOT_URL, new File("testdata").getAbsoluteFile().toURI().toURL().toString());
		
		StringWriter writer = new StringWriter();
		indexer.indexFragment(Collections.singleton(new File("testdata/01-bsn+version.jar")), writer, props);
		
		String expected = Utils.readStream(new FileInputStream("testdata/fragment-subdir1.txt"));
		assertEquals(expected, writer.toString().trim());
	}

	public void testRootInSubSubdirectory() throws Exception {
		BIndex2 indexer = new BIndex2();
		
		Map<String, String> props = new HashMap<String, String>();
		props.put(ResourceIndexer.ROOT_URL, new File("testdata").getAbsoluteFile().toURI().toURL().toString());
		
		StringWriter writer = new StringWriter();
		indexer.indexFragment(Collections.singleton(new File("testdata/subdir/01-bsn+version.jar")), writer, props);
		
		String expected = Utils.readStream(new FileInputStream("testdata/fragment-subdir2.txt"));
		assertEquals(expected, writer.toString().trim());
	}

	public void testLogErrorsFromAnalyzer() throws Exception {
		ResourceAnalyzer badAnalyzer = new ResourceAnalyzer() {
			public void analyzeResource(Resource resource, List<Capability> capabilities, List<Requirement> requirements) throws Exception {
				throw new Exception("Bang!");
			}
		};
		ResourceAnalyzer goodAnalyzer = mock(ResourceAnalyzer.class);
		
		BIndex2 indexer = new BIndex2();
		indexer.addAnalyzer(badAnalyzer, null);
		indexer.addAnalyzer(goodAnalyzer, null);
		
		LogService log = mock(LogService.class);
		indexer.setLog(log);
		
		// Run the indexer
		Map<String, String> props = new HashMap<String, String>();
		props.put(ResourceIndexer.ROOT_URL, new File("testdata").getAbsoluteFile().toURI().toURL().toString());
		StringWriter writer = new StringWriter();
		indexer.indexFragment(Collections.singleton(new File("testdata/subdir/01-bsn+version.jar")), writer, props);
		
		// The "good" analyzer should have been called
		verify(goodAnalyzer).analyzeResource(any(Resource.class), anyListOf(Capability.class), anyListOf(Requirement.class));
		
		// The log service should have been notified about the exception
		ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
		verify(log).log(eq(LogService.LOG_ERROR), any(String.class), exceptionCaptor.capture());
		assertEquals("Bang!", exceptionCaptor.getValue().getMessage());
	}
	
	public void testBundleOutsideRootDirectory() throws Exception {
		BIndex2 indexer = new BIndex2();
		LogService log = mock(LogService.class);
		indexer.setLog(log);
		
		Map<String, String> props = new HashMap<String, String>();
		props.put(ResourceIndexer.ROOT_URL, new File("testdata/subdir").getAbsoluteFile().toURI().toURL().toString());
		
		StringWriter writer = new StringWriter();
		indexer.indexFragment(Collections.singleton(new File("testdata/01-bsn+version.jar")), writer, props);
		
		verify(log).log(eq(LogService.LOG_ERROR), anyString(), isA(IllegalArgumentException.class));
	}
	
	public void testRemoveDisallowed() throws Exception {
		BIndex2 indexer = new BIndex2();
		indexer.addAnalyzer(new NaughtyAnalyzer(), null);
		LogService log = mock(LogService.class);
		indexer.setLog(log);
		
		Map<String, String> props = new HashMap<String, String>();
		props.put(ResourceIndexer.ROOT_URL, new File("testdata").getAbsoluteFile().toURI().toURL().toString());
		
		StringWriter writer = new StringWriter();
		indexer.indexFragment(Collections.singleton(new File("testdata/subdir/01-bsn+version.jar")), writer, props);
		
		verify(log).log(eq(LogService.LOG_ERROR), anyString(), isA(UnsupportedOperationException.class));
	}

}
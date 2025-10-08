// generated from template 'DocHelperTest.ftl' on 2025-10-08T07:27:31.732+02:00
package test.org.fugerit.java.demo.venussampletablecheckintegrity;

import nl.altindag.log.LogCaptor;
import org.apache.fop.fo.ValidationException;
import org.fugerit.java.core.cfg.ConfigRuntimeException;
import org.fugerit.java.core.io.FileIO;
import org.fugerit.java.demo.venussampletablecheckintegrity.DocHelper;
import org.fugerit.java.demo.venussampletablecheckintegrity.People;

import org.fugerit.java.doc.base.config.DocConfig;
import org.fugerit.java.doc.base.feature.DocFeatureRuntimeException;
import org.fugerit.java.doc.base.feature.tableintegritycheck.TableIntegrityCheck;
import org.fugerit.java.doc.base.feature.tableintegritycheck.TableIntegrityCheckConstants;
import org.fugerit.java.doc.base.process.DocProcessContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is a basic example of Fugerit Venus Doc usage,
 * running this junit will :
 * - creates data to be used in document model
 * - renders the 'document.ftl' template
 * - print the result in markdown format
 *
 * For further documentation :
 * https://github.com/fugerit-org/fj-doc
 *
 * NOTE: This is a 'Hello World' style example, adapt it to your scenario, especially :
 *  - change the doc handler and the output mode (here a ByteArrayOutputStream buffer is used)
 */
class DocHelperTest {

    private static final Logger log = LoggerFactory.getLogger( DocHelperTest.class );

    private static final String CHECK_WARN_LOG = "Table Integrity Check FAILED : 2";

    File testDocProcessWorker( String tableCheckIntegrity, List<String> logs ) throws Exception {
        LogCaptor logCaptor = LogCaptor.forClass(TableIntegrityCheck.class);
        try ( ByteArrayOutputStream baos = new ByteArrayOutputStream() ) {
            // creates the doc helper
            DocHelper docHelper = new DocHelper();
            // create custom data for the fremarker template 'document.ftl'
            List<People> listPeople = Arrays.asList( new People( "Luthien", "Tinuviel", "Queen" ), new People( "Thorin", "Oakshield", "King" ) );
            String chainId = "document";
            // handler id
            String handlerId = DocConfig.TYPE_PDF;
            // output generation
            docHelper.getDocProcessConfig().fullProcess( chainId,
                    DocProcessContext.newContext( "listPeople", listPeople )
                            .withAtt( "tableCheckIntegrity", tableCheckIntegrity ),
                    handlerId, baos );
            // print the output
            File outputPdf = new File( String.format( "target/%s-%s.pdf", chainId, tableCheckIntegrity ) );
            log.debug( "delete output file?: {} -> {}", outputPdf, outputPdf.delete() );
            log.info( "{} output : \n{}", handlerId, outputPdf );
            FileIO.writeBytes( baos.toByteArray(), outputPdf );
            return outputPdf;
        } finally {
            logs.addAll( logCaptor.getLogs() );
        }
    }

    @Test
    void testDocProcessDisabled() throws Exception {
        // expected :
        // org.apache.fop.fo.ValidationException: The column-number or number of cells in the row overflows the number of fo:table-columns specified for the table. (See position 71:8)
        List<String> warnLogsCapture = new ArrayList<>();
        try {
            this.testDocProcessWorker( TableIntegrityCheckConstants.TABLE_INTEGRITY_CHECK_DISABLED, warnLogsCapture );
            Assertions.fail( "This test should fail" );
        } catch ( ConfigRuntimeException e ) {
            Throwable origin = e.getCause();
            while ( origin != null && !(origin instanceof ValidationException) ) {
                origin = origin.getCause();
            }
            log.error( e.getCause().getMessage(), e.getCause() );
            Assertions.assertTrue( origin instanceof ValidationException );
        }
        // with table-check-integrity : disabled, logs should NOT contain specific message
        Assertions.assertFalse( warnLogsCapture.contains( CHECK_WARN_LOG ) );
    }

    @Test
    void testDocProcessWarn() throws Exception {
        // expected :
        // org.apache.fop.fo.ValidationException: The column-number or number of cells in the row overflows the number of fo:table-columns specified for the table. (See position 71:8)
        //
        // in log should be something lile :
        // [main] WARN org.fugerit.java.doc.base.feature.tableintegritycheck.TableIntegrityCheck - Table Integrity Check FAILED : 2
        // [main] WARN org.fugerit.java.doc.base.feature.tableintegritycheck.TableIntegrityCheck - Row 1 has 4 columns instead of 3
        // [main] WARN org.fugerit.java.doc.base.feature.tableintegritycheck.TableIntegrityCheck - Row 2 has 4 columns instead of 3
        List<String> warnLogsCapture = new ArrayList<>();
        try {
            this.testDocProcessWorker(TableIntegrityCheckConstants.TABLE_INTEGRITY_CHECK_WARN, warnLogsCapture );
            Assertions.fail( "This test should fail" );
        } catch ( ConfigRuntimeException e ) {
            Throwable origin = e.getCause();
            while ( origin != null && !(origin instanceof ValidationException) ) {
                origin = origin.getCause();
            }
            log.error( e.getCause().getMessage(), e.getCause() );
            Assertions.assertTrue( origin instanceof ValidationException );
        }
        // with table-check-integrity : warn, logs should contain specific message
        Assertions.assertTrue( warnLogsCapture.contains( CHECK_WARN_LOG ) );
    }

    @Test
    void testDocProcessFail() throws Exception {
        // expected :
        // org.fugerit.java.doc.base.feature.DocFeatureRuntimeException: Table check integrity failed, see logs for details.
        List<String> warnLogsCapture = new ArrayList<>();
        try {
            this.testDocProcessWorker(TableIntegrityCheckConstants.TABLE_INTEGRITY_CHECK_FAIL, warnLogsCapture );
            Assertions.fail( "This test should fail" );
        } catch ( DocFeatureRuntimeException e ) {
            log.error( e.getMessage(), e );
            Assertions.assertEquals( "Table check integrity failed, see logs for details.", e.getMessage() );
        }
        // with table-check-integrity : fail, logs should contain specific message
        Assertions.assertTrue( warnLogsCapture.contains( CHECK_WARN_LOG ) );
    }

}

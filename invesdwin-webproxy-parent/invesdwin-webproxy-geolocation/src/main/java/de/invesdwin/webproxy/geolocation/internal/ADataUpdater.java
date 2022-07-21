package de.invesdwin.webproxy.geolocation.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.io.IOUtils;

import de.invesdwin.context.integration.compression.ADecompressingInputStream;
import de.invesdwin.context.log.Log;
import de.invesdwin.util.lang.Closeables;
import de.invesdwin.util.lang.Files;
import de.invesdwin.util.lang.description.TextDescription;
import de.invesdwin.util.lang.uri.URIs;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.date.FDate;
import de.invesdwin.util.time.date.FDates;

@Immutable
public abstract class ADataUpdater {

    protected final Log log = new Log(this);

    /**
     * Downloads the file if it is necessary. It is necessary if:
     * 
     * <ul>
     * <li>The target file does not exist or it is empty.</li> or
     * <li>The target file is older than the max age and a newer file is available at the URL.</li>
     * </ul>
     * 
     * If the target file does not exist or it is empty, the lock gets directly used, otherwise the lock is only used
     * when replacing the target file with the new version.
     */
    protected boolean eventuallyUpdateData(final URL sourceUrl, final File targetFile) throws IOException {
        if (!targetFileAlreadyExists(targetFile) || shouldNewFileBeDownloaded(sourceUrl, targetFile)) {
            updateFile(sourceUrl, targetFile);
            return true;
        }
        return false;
    }

    protected boolean targetFileAlreadyExists(final File targetFile) {
        return targetFile.exists() && Files.sizeOf(targetFile) > 0;
    }

    protected void updateFile(final URL sourceUrl, final File targetFile) throws IOException {
        final File tempTargetFile = new File(targetFile.getAbsolutePath() + ".part");
        try {
            final Instant start = new Instant();
            Files.deleteQuietly(tempTargetFile);
            downloadNewFile(sourceUrl, tempTargetFile);
            replaceFile(targetFile, tempTargetFile);
            log.info("%s successfully updated after %s", targetFile, start);
        } catch (final FileNotFoundException e) {
            Files.deleteQuietly(tempTargetFile);
            throw e;
        } catch (final IOException e) {
            Files.deleteQuietly(tempTargetFile);
            throw e;
        }
    }

    protected void replaceFile(final File targetFile, final File tempTargetFile) throws IOException {
        log.info("%s successfully downloaded, now replacing %s with the new version", tempTargetFile, targetFile);
        Files.deleteQuietly(targetFile);
        Files.moveFile(tempTargetFile, targetFile);

    }

    protected boolean shouldNewFileBeDownloaded(final URL sourceUrl, final File targetFile) throws IOException {
        final long sourceLastModified = URIs.connect(sourceUrl).lastModified();
        final boolean thereIsANewSourceFile = Files.isFileOlder(targetFile, sourceLastModified);
        final boolean currentFileIsFromLastMonth = !FDates.isSameMonth(new FDate(targetFile.lastModified()),
                new FDate());
        return thereIsANewSourceFile && currentFileIsFromLastMonth;
    }

    protected void downloadNewFile(final URL sourceUrl, final File targetFile) throws IOException {
        Files.forceMkdir(targetFile.getParentFile());
        log.info("Downloading %s to %s", sourceUrl, targetFile);
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new ADecompressingInputStream(new TextDescription("%s: downloadNewFile(%s, %s)",
                    ADataUpdater.class.getSimpleName(), sourceUrl, targetFile)) {
                @Override
                protected InputStream innerNewDelegate() {
                    try {
                        return URIs.connect(sourceUrl).downloadInputStream();
                    } catch (final IOException e) {
                        throw new TransparentRuntimeIOExeption(e);
                    }
                }
            };
            out = new FileOutputStream(targetFile, false);
            IOUtils.copy(in, out);
        } catch (final Throwable t) {
            if (t instanceof TransparentRuntimeIOExeption) {
                final TransparentRuntimeIOExeption rio = (TransparentRuntimeIOExeption) t;
                throw rio.getCause();
            } else {
                throw t;
            }
        } finally {
            Closeables.closeQuietly(in);
            Closeables.closeQuietly(out);
        }
    }

    private static class TransparentRuntimeIOExeption extends RuntimeException {

        private static final long serialVersionUID = 1L;

        TransparentRuntimeIOExeption(final IOException cause) {
            super(cause);
        }

        @Override
        public synchronized IOException getCause() {
            return (IOException) super.getCause();
        }

    }

}

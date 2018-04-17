package com.aeplace.moneybag.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitUser;
import org.eclipse.egit.github.core.Reference;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.Tree;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.TypedResource;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import com.aeplace.moneybag.value.EncounterForm;
import com.google.gson.Gson;

public class Utility {

	private static HashMap<String, String> cards;
	public static final int cardWidth = 500;
	public static final int cardHeight = 700;

	static {
		cards = new HashMap<String, String>();
	}

	public static boolean submitCard(String name, String image) throws IOException {

		if (cards.containsKey(name)) {
			try {
				if (saveCardData(name, cards.get(name)) == false) {
					return false;
				}

				String username = "alex-place";
				String repoName = "moneybags";
				GitHubClient client = new GitHubClient();
				String oath = System.getenv("java-api");
				client.setOAuth2Token(oath);

				// create needed services
				RepositoryService repositoryService = new RepositoryService();
				CommitService commitService = new CommitService(client);
				DataService dataService = new DataService(client);

				// get some sha's from current state in git
				Repository repository = repositoryService.getRepository(username, repoName);
				String baseCommitSha = repositoryService.getBranches(repository).get(0).getCommit().getSha();
				RepositoryCommit baseCommit = commitService.getCommit(repository, baseCommitSha);
				String treeSha = baseCommit.getSha();

				// create new blob with data
				Blob blob = new Blob();
				blob.setContent(image).setEncoding(Blob.ENCODING_BASE64);
				String blob_sha = dataService.createBlob(repository, blob);
				Tree baseTree = dataService.getTree(repository, treeSha);

				// create new tree entry
				TreeEntry treeEntry = new TreeEntry();
				treeEntry.setPath("images/card_" + name + ".png");
				treeEntry.setMode(TreeEntry.MODE_BLOB);
				treeEntry.setType(TreeEntry.TYPE_BLOB);
				treeEntry.setSha(blob_sha);
				treeEntry.setSize(blob.getContent().length());
				Collection<TreeEntry> entries = new ArrayList<TreeEntry>();
				entries.add(treeEntry);
				Tree newTree = dataService.createTree(repository, entries, baseTree.getSha());

				// create commit
				Commit commit = new Commit();
				commit.setMessage(name + ": User submitted image at " + new Date(System.currentTimeMillis()).toLocaleString());
				commit.setTree(newTree);

				UserService userService = new UserService(client);
				User user = userService.getUser();
				CommitUser author = new CommitUser();
				author.setName(user.getName());
				author.setName(username);
				author.setEmail("alex.place.7@gmail.com");
				Calendar now = Calendar.getInstance();
				author.setDate(now.getTime());
				commit.setAuthor(author);
				commit.setCommitter(author);

				List<Commit> listOfCommits = new ArrayList<Commit>();
				listOfCommits.add(new Commit().setSha(baseCommitSha));
				// listOfCommits.containsAll(base_commit.getParents());
				commit.setParents(listOfCommits);
				// commit.setSha(base_commit.getSha());
				Commit newCommit = dataService.createCommit(repository, commit);

				// create resource
				TypedResource commitResource = new TypedResource();
				commitResource.setSha(newCommit.getSha());
				commitResource.setType(TypedResource.TYPE_COMMIT);
				commitResource.setUrl(newCommit.getUrl());

				// get master reference and update it
				Reference reference = dataService.getReference(repository, "heads/master");
				reference.setObject(commitResource);
				dataService.editReference(repository, reference, true);
				System.out.println("Committed URL: " + newCommit.getUrl());
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	public static void addToMap(EncounterForm encounter) {
		Gson gson = new Gson();
		String json = gson.toJson(encounter);
		cards.put(encounter.getName(), json);
	}

	public static boolean saveCardData(String name, String json) throws IOException {
		try {
			String username = "alex-place";
			String repoName = "moneybags";
			GitHubClient client = new GitHubClient();
			String oath = System.getenv("java-api");
			client.setOAuth2Token(oath);

			// create needed services
			RepositoryService repositoryService = new RepositoryService();
			CommitService commitService = new CommitService(client);
			DataService dataService = new DataService(client);

			// get some sha's from current state in git
			Repository repository = repositoryService.getRepository(username, repoName);
			String baseCommitSha = repositoryService.getBranches(repository).get(0).getCommit().getSha();
			RepositoryCommit baseCommit = commitService.getCommit(repository, baseCommitSha);
			String treeSha = baseCommit.getSha();

			// create new blob with data
			Blob blob = new Blob();
			blob.setContent(json).setEncoding(Blob.ENCODING_UTF8);
			String blob_sha = dataService.createBlob(repository, blob);
			Tree baseTree = dataService.getTree(repository, treeSha);

			// create new tree entry
			TreeEntry treeEntry = new TreeEntry();
			treeEntry.setPath("data/card_" + name + ".json");
			treeEntry.setMode(TreeEntry.MODE_BLOB);
			treeEntry.setType(TreeEntry.TYPE_BLOB);
			treeEntry.setSha(blob_sha);
			treeEntry.setSize(blob.getContent().length());
			Collection<TreeEntry> entries = new ArrayList<TreeEntry>();
			entries.add(treeEntry);
			Tree newTree = dataService.createTree(repository, entries, baseTree.getSha());

			// create commit
			Commit commit = new Commit();
			commit.setMessage("User submitted data at " + new Date(System.currentTimeMillis()).toLocaleString());
			commit.setTree(newTree);

			UserService userService = new UserService(client);
			User user = userService.getUser();
			CommitUser author = new CommitUser();
			author.setName(user.getName());
			author.setName(username);
			author.setEmail("alex.place.7@gmail.com");
			Calendar now = Calendar.getInstance();
			author.setDate(now.getTime());
			commit.setAuthor(author);
			commit.setCommitter(author);

			List<Commit> listOfCommits = new ArrayList<Commit>();
			listOfCommits.add(new Commit().setSha(baseCommitSha));
			// listOfCommits.containsAll(base_commit.getParents());
			commit.setParents(listOfCommits);
			// commit.setSha(base_commit.getSha());
			Commit newCommit = dataService.createCommit(repository, commit);

			// create resource
			TypedResource commitResource = new TypedResource();
			commitResource.setSha(newCommit.getSha());
			commitResource.setType(TypedResource.TYPE_COMMIT);
			commitResource.setUrl(newCommit.getUrl());

			// get master reference and update it
			Reference reference = dataService.getReference(repository, "heads/master");
			reference.setObject(commitResource);
			dataService.editReference(repository, reference, true);
			System.out.println("Committed URL: " + newCommit.getUrl());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static String compileCards(String path) throws IOException, URISyntaxException {
		boolean result = false;
		int width = 5000;
		int height = 4900;
		int xShift = 500;
		int yShift = 700;
		BufferedImage combined = new BufferedImage(5000, 4900, BufferedImage.TYPE_INT_ARGB);
		Graphics g = combined.getGraphics();

		path = "images";

		File[] files = new File(path).listFiles();
		// If this pathname does not denote a directory, then listFiles()
		// returns null.
		if (files == null || files.length == 0) {
			return null;
		}

		ArrayList<BufferedImage> images = new ArrayList<>();

		for (File file : files) {
			if (file.isFile()) {
				if (getFileExtension(file).equalsIgnoreCase("png")) {
					BufferedImage buffImage = ImageIO.read(file);
					resize(buffImage, cardWidth, cardHeight);
					images.add(resize(buffImage, cardWidth, cardHeight));
				}
			}
		}

		Iterator<BufferedImage> iterator = images.iterator();

		// First card must be the card back which gets drawn at the bottom right
		// of the template
		g.drawImage(iterator.next(), xShift * 9, yShift * 6, null);

		for (int y = 0; y < 7; y++) {
			for (int x = 0; x < 10; x++) {

				if (iterator.hasNext()) {
					BufferedImage image = iterator.next();
					g.drawImage(image, x * xShift, y * yShift, null);
				}
			}
		}
		g.dispose();

		return imgToBase64String(combined, "png");
	}

	public static String imgToBase64String(final RenderedImage img, final String formatName) {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			ImageIO.write(img, formatName, os);
			return Base64.getEncoder().encodeToString(os.toByteArray());
		} catch (final IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

	public static void submitDeck(String deckName, String encode) throws IOException {

		String username = "alex-place";
		String repoName = "moneybags";
		GitHubClient client = new GitHubClient();
		String oath = System.getenv("java-api");
		client.setOAuth2Token(oath);

		// create needed services
		RepositoryService repositoryService = new RepositoryService();
		CommitService commitService = new CommitService(client);
		DataService dataService = new DataService(client);

		// get some sha's from current state in git
		Repository repository = repositoryService.getRepository(username, repoName);
		String baseCommitSha = repositoryService.getBranches(repository).get(0).getCommit().getSha();
		RepositoryCommit baseCommit = commitService.getCommit(repository, baseCommitSha);
		String treeSha = baseCommit.getSha();

		// create new blob with data
		Blob blob = new Blob();
		blob.setContent(encode).setEncoding(Blob.ENCODING_BASE64);
		String blob_sha = dataService.createBlob(repository, blob);
		Tree baseTree = dataService.getTree(repository, treeSha);

		// create new tree entry
		TreeEntry treeEntry = new TreeEntry();
		treeEntry.setPath("decks/" + deckName + ".png");
		treeEntry.setMode(TreeEntry.MODE_BLOB);
		treeEntry.setType(TreeEntry.TYPE_BLOB);
		treeEntry.setSha(blob_sha);
		treeEntry.setSize(blob.getContent().length());
		Collection<TreeEntry> entries = new ArrayList<TreeEntry>();
		entries.add(treeEntry);
		Tree newTree = dataService.createTree(repository, entries, baseTree.getSha());

		// create commit
		Commit commit = new Commit();
		commit.setMessage("System submitted deck at " + new Date(System.currentTimeMillis()).toLocaleString());
		commit.setTree(newTree);

		UserService userService = new UserService(client);
		User user = userService.getUser();
		CommitUser author = new CommitUser();
		author.setName(user.getName());
		author.setName(username);
		author.setEmail("alex.place.7@gmail.com");
		Calendar now = Calendar.getInstance();
		author.setDate(now.getTime());
		commit.setAuthor(author);
		commit.setCommitter(author);

		List<Commit> listOfCommits = new ArrayList<Commit>();
		listOfCommits.add(new Commit().setSha(baseCommitSha));
		// listOfCommits.containsAll(base_commit.getParents());
		commit.setParents(listOfCommits);
		// commit.setSha(base_commit.getSha());
		Commit newCommit = dataService.createCommit(repository, commit);

		// create resource
		TypedResource commitResource = new TypedResource();
		commitResource.setSha(newCommit.getSha());
		commitResource.setType(TypedResource.TYPE_COMMIT);
		commitResource.setUrl(newCommit.getUrl());

		// get master reference and update it
		Reference reference = dataService.getReference(repository, "heads/master");
		reference.setObject(commitResource);
		dataService.editReference(repository, reference, true);
		System.out.println("Committed URL: " + newCommit.getUrl());
	}

	public static String getFileExtension(File file) {
		String filename = file.getName();
		int i = filename.lastIndexOf('.');
		if (i > 0) {
			return filename.substring(i + 1);
		}
		return null;
	}

	public static BufferedImage resize(BufferedImage img, int newW, int newH) {
		Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
		BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		return dimg;
	}

}

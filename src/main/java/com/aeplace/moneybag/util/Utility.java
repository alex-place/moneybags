package com.aeplace.moneybag.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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

public class Utility {
	
	static{
		
		
		
	}

	public static void submitCard(String image) throws IOException{
		String username = "alex-place";
		String repoName = "moneybags";
		GitHubClient client = new GitHubClient();
		client.setOAuth2Token("bc82ad356013362013367ab38f3303d5b94e6ba1");

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
		treeEntry.setPath("images/card_" + System.currentTimeMillis() + ".png");
		treeEntry.setMode(TreeEntry.MODE_BLOB);
		treeEntry.setType(TreeEntry.TYPE_BLOB);
		treeEntry.setSha(blob_sha);
		treeEntry.setSize(blob.getContent().length());
		Collection<TreeEntry> entries = new ArrayList<TreeEntry>();
		entries.add(treeEntry);
		Tree newTree = dataService.createTree(repository, entries, baseTree.getSha());

		// create commit
		Commit commit = new Commit();
		commit.setMessage("first commit at " + new Date(System.currentTimeMillis()).toLocaleString());
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
	
}

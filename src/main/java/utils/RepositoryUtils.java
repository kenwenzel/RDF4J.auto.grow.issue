package utils;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryProvider;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.sail.lmdb.config.LmdbStoreConfig;


/** Utilities for repositories to be accessed over a RDF4J server */
public class RepositoryUtils {
  private final RepositoryManager repositoryManager;


  /** Generates a new {@link RepositoryUtils} and primes the connection to the RDF4J Server */
  public RepositoryUtils(String rdf4jServerURL) {
    // uses RepositoryProvider to utilize a builtin shutdown hook
    this.repositoryManager = RepositoryProvider.getRepositoryManager(rdf4jServerURL);
    this.repositoryManager.init();
  }

  /**
   * Returns a {@link Repository}, if it exists
   *
   * @param repositoryId Id of repository
   * @return {@link Repository} or {@code null}, if it does not exist
   * @throws RepositoryConfigException If no {@link Repository} could be created due to invalid or
   *     incomplete configuration data.
   */
  public Repository getRepository(String repositoryId)
      throws RepositoryConfigException {
    return this.repositoryManager.getRepository(repositoryId);
  }

  /**
   * Creates a new repository with the given params. It will generate a new {@link RepositoryConfig}
   * for it and add it.ﬂ
   *
   * @param repositoryId Id of repository
   * @throws IllegalArgumentException If the given params are already a repo
   */
  public void createRepository(String repositoryId)
      throws IllegalArgumentException {
    if (getRepository(repositoryId) != null) {
      throw new IllegalArgumentException("Repository already exists.");
    }

    // add config repo database
    // the repo will be auto created if a config exist for it, and it's called the first time
    LmdbStoreConfig lmdbConfig = new LmdbStoreConfig();

    SailRepositoryConfig sailConfig = new SailRepositoryConfig(lmdbConfig);
    RepositoryConfig repositoryConfig = new RepositoryConfig(repositoryId, sailConfig);
    this.repositoryManager.addRepositoryConfig(repositoryConfig);
  }

  /**
   * Deletes a repository with the given params
   *
   * @param repositoryId Id of the repository
   * @throws IllegalArgumentException If the given params are not a valid repository
   */
  public void deleteRepository(String repositoryId)
      throws IllegalArgumentException {
    if (!this.repositoryManager.removeRepository(repositoryId)) {
      throw new IllegalArgumentException("Repository not found.");
    }
  }

  /**
   * Checks if a given repo exists
   *
   * @param repositoryId Id of repository
   * @return {@code True} if the repo exists, otherwise {@code False}
   * @throws RepositoryConfigException If no {@link Repository} could be created due to invalid or
   *     incomplete configuration data.
   */
  public boolean exists(String repositoryId) throws RepositoryConfigException {
    Repository repo = getRepository(repositoryId);
    if (repo != null) {
      repo.shutDown(); // exist should not keep repo initialized
      return true;
    } else {
      return false;
    }
  }

  /**
   * Generates a {@link RepositoryConnection} for a repository, with the given params
   *
   * @param repositoryId Id of repository
   * @return {@link RepositoryConnection} if it exists, otherwise {@code null}
   * @throws RepositoryConfigException If no {@link Repository} could be created due to invalid or
   *     incomplete configuration data.
   */
  public RepositoryConnection getConnection(String repositoryId)
      throws RepositoryConfigException {
    Repository repo = getRepository(repositoryId);
    if (repo != null) {
      return repo.getConnection();
    } else {
      return null;
    }
  }
}

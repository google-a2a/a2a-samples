import os
from datetime import datetime, timedelta
from typing import Dict, Any, Optional, List

from github import Github, Auth
from pydantic import BaseModel


class GitHubUser(BaseModel):
    """GitHub user information"""
    login: str
    name: Optional[str] = None
    email: Optional[str] = None


class GitHubRepository(BaseModel):
    """GitHub repository information"""
    name: str
    full_name: str
    description: Optional[str] = None
    url: str
    updated_at: str
    pushed_at: Optional[str] = None
    language: Optional[str] = None
    stars: int
    forks: int
    
    
class GitHubCommit(BaseModel):
    """GitHub commit information"""
    sha: str
    message: str
    author: str
    date: str
    url: str


class GitHubResponse(BaseModel):
    """Base response model for GitHub API operations"""
    status: str
    message: str
    count: Optional[int] = None
    error_message: Optional[str] = None


class RepositoryResponse(GitHubResponse):
    """Response model for repository operations"""
    data: Optional[List[GitHubRepository]] = None


class CommitResponse(GitHubResponse):
    """Response model for commit operations"""
    data: Optional[List[GitHubCommit]] = None


class GitHubToolset:
    """GitHub API toolset for querying repositories and recent updates"""
    
    def __init__(self):
        self._github_client = None
    
    def _get_github_client(self) -> Github:
        """Get GitHub client with authentication"""
        if self._github_client is None:
            github_token = os.getenv('GITHUB_TOKEN')
            if github_token:
                auth = Auth.Token(github_token)
                self._github_client = Github(auth=auth)
            else:
                # Use without authentication (limited rate)
                print("Warning: No GITHUB_TOKEN found, using unauthenticated access (limited rate)")
                self._github_client = Github()
        return self._github_client
    
    def get_user_repositories(self, username: Optional[str] = None, days: Optional[int] = None, limit: Optional[int] = None) -> RepositoryResponse:
        """Get user's repositories with recent updates
        Args:
            username: GitHub username (optional, defaults to authenticated user)
            days: Number of days to look for recent updates (default: 30 days)
            limit: Maximum number of repositories to return (default: 10)
            
        Returns:
            RepositoryResponse: Contains status, repository list, and metadata
        """
        # Set default values
        if days is None:
            days = 30
        if limit is None:
            limit = 10

        try:
            github = self._get_github_client()
            
            if username:
                user = github.get_user(username)
            else:
                try:
                    user = github.get_user()
                except Exception:
                    # If no token, we can't get authenticated user, so require username
                    return RepositoryResponse(
                        status='error',
                        message='Username is required when not using authentication token',
                        error_message='Username is required when not using authentication token'
                    )
            
            repos = []
            cutoff_date = datetime.now() - timedelta(days=days)
            
            for repo in user.get_repos(sort='updated', direction='desc'):
                if len(repos) >= limit:
                    break
                    
                if repo.updated_at >= cutoff_date:
                    repos.append(GitHubRepository(
                        name=repo.name,
                        full_name=repo.full_name,
                        description=repo.description,
                        url=repo.html_url,
                        updated_at=repo.updated_at.isoformat(),
                        pushed_at=repo.pushed_at.isoformat() if repo.pushed_at else None,
                        language=repo.language,
                        stars=repo.stargazers_count,
                        forks=repo.forks_count
                    ))
            
            return RepositoryResponse(
                status='success',
                data=repos,
                count=len(repos),
                message=f'Successfully retrieved {len(repos)} repositories updated in the last {days} days'
            )
        except Exception as e:
            return RepositoryResponse(
                status='error',
                message=f'Failed to get repositories: {str(e)}',
                error_message=f'Failed to get repositories: {str(e)}'
            )
    
    def get_recent_commits(self, repo_name: str, days: Optional[int] = None, limit: Optional[int] = None) -> CommitResponse:
        """Get recent commits for a repository
        
        Args:
            repo_name: Repository name in format 'owner/repo'
            days: Number of days to look for recent commits (default: 7 days)
            limit: Maximum number of commits to return (default: 10)
            
        Returns:
            CommitResponse: Contains status, commit list, and metadata
        """
        # Set default values
        if days is None:
            days = 7
        if limit is None:
            limit = 10
            
        try:
            github = self._get_github_client()
            
            repo = github.get_repo(repo_name)
            commits = []
            cutoff_date = datetime.now() - timedelta(days=days)
            
            for commit in repo.get_commits(since=cutoff_date):
                if len(commits) >= limit:
                    break
                    
                commits.append(GitHubCommit(
                    sha=commit.sha[:8],
                    message=commit.commit.message.split('\n')[0],  # Only take the first line
                    author=commit.commit.author.name,
                    date=commit.commit.author.date.isoformat(),
                    url=commit.html_url
                ))
            
            return CommitResponse(
                status='success',
                data=commits,
                count=len(commits),
                message=f'Successfully retrieved {len(commits)} commits for repository {repo_name} in the last {days} days'
            )
        except Exception as e:
            return CommitResponse(
                status='error',
                message=f'Failed to get commits: {str(e)}',
                error_message=f'Failed to get commits: {str(e)}'
            )
    
    def search_repositories(self, query: str, sort: Optional[str] = None, limit: Optional[int] = None) -> RepositoryResponse:
        """Search for repositories with recent activity
        
        Args:
            query: Search query string
            sort: Sorting method, optional values: 'updated', 'stars', 'forks' (default: 'updated')
            limit: Maximum number of repositories to return (default: 10)
            
        Returns:
            RepositoryResponse: Contains status, search results, and metadata
        """
        # Set default values
        if sort is None:
            sort = 'updated'
        if limit is None:
            limit = 10
            
        try:
            github = self._get_github_client()
            
            # Add recent activity filter to query
            search_query = f"{query} pushed:>={datetime.now() - timedelta(days=30):%Y-%m-%d}"
            
            repos = []
            results = github.search_repositories(query=search_query, sort=sort, order='desc')
            
            for repo in results[:limit]:
                repos.append(GitHubRepository(
                    name=repo.name,
                    full_name=repo.full_name,
                    description=repo.description,
                    url=repo.html_url,
                    updated_at=repo.updated_at.isoformat(),
                    pushed_at=repo.pushed_at.isoformat() if repo.pushed_at else None,
                    language=repo.language,
                    stars=repo.stargazers_count,
                    forks=repo.forks_count
                ))
            
            return RepositoryResponse(
                status='success',
                data=repos,
                count=len(repos),
                message=f'Successfully searched for {len(repos)} repositories matching "{query}"'
            )
        except Exception as e:
            return RepositoryResponse(
                status='error',
                message=f'Failed to search repositories: {str(e)}',
                error_message=f'Failed to search repositories: {str(e)}'
            )
    
    def get_tools(self) -> Dict[str, Any]:
        """Return dictionary of available tools for OpenAI function calling"""
        return {
            'get_user_repositories': self,
            'get_recent_commits': self,
            'search_repositories': self,
        } 
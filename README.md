# Radar-Dating-Android

To show ads generate random number between 3-5 and then show ads on Recommendations screen, 
Ads only be visible on Recommendation screen and While viewing Singles in hotspot

Shoot your shot(Send like with message)->
1. Profile Screen
2. Recommendation screen
3. Near You - in-popups

# How we work on Github:
``
Reference Link: https://buddy.works/blog/5-types-of-git-workflows

- Master is golden. only the merge master touches it (more on this in a bit).

- Develop branch, taken initially from the master, that all devs work off. Instead of having a
  branch per developer, we make feature, or ticket, branches from Develop. For every discreet
  feature (bug, enhancement, etc.), a new local branch is made from Develop. Developers don't have
  to work on the same branch since each feature branch is scoped to only what that single developer
  is working on. This is where it's cheap branching comes in handy. Once the feature is ready, it's
  merged locally back into Develop and pushed up to the cloud (Github). - - Everyone keeps in sync
  by pulling on Develop often.

- Release branch - We work on sprint basis, each sprint will be of 2 weeks. After every sprint, QA
  approves the Develop branch, a Release branch is made with the date in the name. That is the
  branch used in production, replacing last sprint's release branch. Once the Release branch is
  verified by QA in production, the release branch is merged back into master (and dev, just to be
  safe). This is the only time we touch master, ensuring that it is as clean as possible.


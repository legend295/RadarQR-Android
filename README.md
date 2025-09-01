# Radar-Dating-Android

RadarQR is not an event, it’s a lifestyle! No more awkward singles events or shallow swiping! Take control of your matches by inviting people who are on your radar in the real-world… to your dating profile on RadarQR!

**Here’s how it works / Why We’re Different:** 

**QR to Profile:**  We help you sort the people who like you IN REAL LIFE & ONLINE! Each profile comes with a unique QR code. Slap it on shirts, stickers, social media—you name it! Each new scan in the real-world equals a new “LIKE” at your fingertips! Live life, be real, and let the LIKES roll in as you go!

**Hot Spots:**  Activate your SUPER-Powers and know where to connect in real-time with real people, EVERYDAY of the week! With our Hot Spots map, we show you WHERE the singles-hangouts are (bars, coffee shops, gyms, grocery stores, festivals, and more). Once you arrive and check yourself in, you’ll instantly see a list of WHO in the room is also seeking to connect! LOVE, Friends, and More!

**Gen-AI:**  No other platform seamlessly combines Actual Intelligence with Artificial Intelligence to elevate the quality of in-person connections. Introducing “RAD,” your Gen-AI Best Friend. RAD offers tailored recommendations designed to enrich your social interactions, boosting your confidence in real-world situations!

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



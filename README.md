Custom Images
=========

Custom Images Critterz fork, made for MultiPaper cross-server compatibility and in-game NFT image purchases.
Uses critterz-core for internal use.

Commands
---------

- /nft create <contract address> <token id> - creates a new NFT paste. Permission node: 'images.command.create'
- /nft delete - deletes an existing NFT image. Permission node: 'images.command.delete'

Compiling
---------

1. Ensure that you have the project setup properly with Maven
2. Compile from the parent project of Images that all modules are contained within
3. Run `mvn clean package`

Contributing
------------

We happily accept contributions, especially through pull requests on GitHub.
Submissions must be licensed under the MIT License.

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for important guidelines to follow.

Links
-----

* [Visit our plugin page](https://www.spigotmc.org/resources/custom-images.53036/)
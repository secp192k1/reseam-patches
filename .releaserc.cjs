module.exports = {
    branches: ["main"],
    plugins: [
        "@semantic-release/commit-analyzer",
        "@semantic-release/release-notes-generator",
        ["@semantic-release/npm", {npmPublish: false}],
        "./.github/scripts/release.cjs",
        [
            "@semantic-release/git",
            {
                assets: ["package.json", "README.md"],
                message: "chore(release): ${nextRelease.version} [skip ci]\n\n${nextRelease.notes}",
            },
        ],
    ],
};

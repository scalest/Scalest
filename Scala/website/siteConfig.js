/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

const users = [];

const siteConfig = {
    title: 'Scalest', // Title for your website.
    tagline: 'Productivity oriented helpers for web development',
    url: 'https://scalest.github.io/', // Your website URL
    baseUrl: '/Scalest/', // Base URL for your project */
    customDocsPath: "scalest-docs/target/mdoc",
    projectName: 'Scalest',
    organizationName: 'scalest',
    headerLinks: [
        {doc: 'getting_started', label: 'Getting Started'},
        {page: 'help', label: 'Help'},
        {blog: true, label: 'Blog'},
    ],
    users,
    headerIcon: 'img/favicon.ico',
    footerIcon: 'img/favicon.ico',
    favicon: 'img/favicon.ico',
    colors: {
        primaryColor: '#295c0d',
        secondaryColor: '#1c4009',
    },
    copyright: `Copyright © ${new Date().getFullYear()} scalest`,
    highlight: {
        theme: 'default',
    },
    scripts: ['https://buttons.github.io/buttons.js'],
    onPageNav: 'separate',
    cleanUrl: true,
    ogImage: 'img/undraw_online.svg',
    twitterImage: 'img/undraw_tweetstorm.svg',
    repoUrl: 'https://github.com/scalest/Scalest',
};

module.exports = siteConfig;

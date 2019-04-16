db.f_categ.drop();
db.f_categ.insert({
  _id: 'general-okey-discussion',
  name: 'Genel Okey Oyunu Tartışması',
  desc: 'Genel okey oyunu konularını tartışma yeri',
  pos: NumberInt(1),
  nbTopics: NumberInt(0),
  nbPosts: NumberInt(0),
  lastPostId: ''
});
db.f_categ.insert({
  _id: 'oyunkeyf-feedback',
  name: 'Oyunkeyf Geribildirim',
  desc: 'Hata raporları, özellik istekleri, öneriler',
  pos: NumberInt(2),
  nbTopics: NumberInt(0),
  nbPosts: NumberInt(0),
  lastPostId: ""
});
db.f_categ.insert({
  _id: 'off-topic-discussion',
  name: 'Konu dışı Tartışması',
  desc: 'Okey oyunuyla ilgili olmayan herşey',
  pos: NumberInt(3),
  nbTopics: NumberInt(0),
  nbPosts: NumberInt(0),
  lastPostId: ""
});

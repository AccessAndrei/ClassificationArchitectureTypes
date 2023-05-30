import matplotlib.pyplot as plt
import numpy as np
from keras.models import load_model
from keras.preprocessing import image
from tensorflow.keras.preprocessing import image
import tensorflow as tf
from keras.preprocessing.image import ImageDataGenerator

model = load_model('112>111.h5')

# Загрузка тестового изображения
img_path = '/Users/moneygrind/Desktop/image1-6.jpeg'
img = image.load_img(img_path, target_size=(250, 250))
x = image.img_to_array(img)
x = np.expand_dims(x, axis=0)
x /= 255.

# Предсказание класса изображения
preds = model.predict(x)
class_idx = np.argmax(preds)
print(class_idx)
print(preds)
class_name = ['baroque', 'classicism', 'deconstructivism', 'deconstructivism','gothic', 'modernism'][class_idx]

# Вывод изображения и предсказанного класса
plt.imshow(img)
plt.title(class_name)
plt.show()



# загружаем тестовое изображение и преобразуем его в массив NumPy
img = tf.keras.preprocessing.image.load_img(img_path, target_size=(250,250))
x = tf.keras.preprocessing.image.img_to_array(img)
x = np.expand_dims(x, axis=0)

# получаем карты признаков для всех слоев
features = []
for layer in model.layers:
    x = layer(x)
    features.append(x)

# выводим карты признаков для каждого слоя
fig, axs = plt.subplots(nrows=1, ncols=len(features), figsize=(16, 4))

for i, feat in enumerate(features):

    if feat.ndim == 4:
        axs[i].set_title(i)
        axs[i].imshow(feat[0, :, :, 0], cmap='gray')
        axs[i].axis('off')


plt.tight_layout()
plt.show()





